package com.wafflestudio.snutt.timetables.job

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.timetables.data.Timetable
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import io.github.resilience4j.kotlin.ratelimiter.RateLimiterConfig
import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.kotlin.ratelimiter.rateLimiter
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.transaction.PlatformTransactionManager
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 대표 시간표 자동 지정
 */
@Configuration
class AutoPrimaryJobConfig(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val timetableRepository: TimetableRepository,
) {
    @Bean
    fun primaryTimetableAutoSetJob(
        jobRepository: JobRepository,
        primaryTimetableAutoSetStep: Step,
    ): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(primaryTimetableAutoSetStep)
            .build()

    @Bean
    @JobScope
    fun primaryTimetableAutoSetStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        @Value("#{jobParameters[year]}") year: Int,
    ): Step =
        StepBuilder(STEP_NAME, jobRepository)
            .tasklet(
                { _, _ ->
                    autoSetPrimaryTimetable(year)
                    RepeatStatus.FINISHED
                },
                transactionManager,
            ).build()

    data class AggResult(
        val id: Key,
    )

    data class Key(
        val user_id: String,
        val semester: Semester,
        val year: Int,
    )

    private fun autoSetPrimaryTimetable(year: Int) =
        runBlocking {
            val counter = AtomicInteger()
            val timetablesCount =
                reactiveMongoTemplate
                    .count(
                        Query.query(
                            Criteria
                                .where("_id")
                                .ne(null)
                                .and("year")
                                .`is`(year),
                        ),
                        Timetable::class.java,
                    ).block() ?: 0L

            val rateLimiter =
                RateLimiter.of(
                    "autoSetPrimaryTimetable",
                    RateLimiterConfig {
                        limitRefreshPeriod(Duration.ofSeconds(1))
                        limitForPeriod(500)
                        timeoutDuration(Duration.ofMinutes(1))
                    },
                )

            val agg =
                Aggregation.newAggregation(
                    Key::class.java,
                    Aggregation.match(Criteria.where("year").`is`(year)),
                    Aggregation.group("user_id", "semester", "year"),
                )
            val buffer = ConcurrentHashMap.newKeySet<String>()
            reactiveMongoTemplate
                .aggregate(agg, "timetables", AggResult::class.java)
                .asFlow()
                .collect {
                    val primaryTable =
                        rateLimiter.executeSuspendFunction {
                            autoSetPrimary(it.id, counter, timetablesCount)
                        } ?: return@collect

                    buffer.add(primaryTable)
                    if (buffer.size != BULK_WRITE_SIZE) {
                        return@collect
                    }

                    val ids = HashSet(buffer).also { buffer.clear() }
                    launch {
                        reactiveMongoTemplate
                            .bulkOps(
                                BulkOperations.BulkMode.ORDERED,
                                "timetables",
                            ).updateMulti(
                                Query.query(Criteria.where("_id").`in`(ids)),
                                Update.update("is_primary", true),
                            ).execute()
                            .block()
                    }.join()
                    log.info("updated ${ids.size} docs")
                }

            if (buffer.isNotEmpty()) {
                reactiveMongoTemplate
                    .bulkOps(
                        BulkOperations.BulkMode.ORDERED,
                        "timetables",
                    ).updateMulti(
                        Query.query(Criteria.where("_id").`in`(buffer)),
                        Update.update("is_primary", true),
                    ).execute()
                    .block()
            }
        }

    private suspend fun autoSetPrimary(
        key: Key,
        counter: AtomicInteger,
        timetablesCount: Long,
    ): String? {
        val (userId, semester, year) = key
        val timetables = timetableRepository.findAllByUserIdAndYearAndSemester(userId, year, semester).toList()
        if (timetables.any { it.isPrimary == true }) {
            log.info("[${counter.addAndGet(timetables.size)}/$timetablesCount] SKIPPED $key | primary table alrady exists")
            return null
        }

        val newPrimaryTimetable = timetables.maxBy { it.updatedAt }
        log.info("[${counter.addAndGet(timetables.size)}/$timetablesCount] UPDATING $key | primary table ID : ${newPrimaryTimetable.id}")
        return newPrimaryTimetable.id
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private const val BULK_WRITE_SIZE = 100
        const val JOB_NAME = "primaryTimetableAutoSetJob"
        const val STEP_NAME = "primaryTimetableAutoSetStep"
    }
}
