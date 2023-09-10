package com.wafflestudio.snu4t.timetables.job

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import io.github.resilience4j.kotlin.ratelimiter.rateLimiter
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.transaction.PlatformTransactionManager
import java.time.Duration

/**
 * 대표 시간표 자동 지정
 */
@Configuration
class AutoPrimaryJobConfig(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val timetableRepository: TimetableRepository
) {

    @Bean
    fun primaryTimetableAutoSetJob(jobRepository: JobRepository, primaryTimetableAutoSetStep: Step): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(primaryTimetableAutoSetStep)
            .build()
    }

    @Bean
    fun primaryTimetableAutoSetStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step = StepBuilder(STEP_NAME, jobRepository).tasklet(
        { _, _ ->
            autoSetPrimaryTimetable()
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()

    data class AggResult(val id: Key)
    data class Key(val user_id: String, val semester: Semester, val year: Int)
    private fun autoSetPrimaryTimetable() = runBlocking {
        val rateLimiter = RateLimiter.ofDefaults("")
        val agg = Aggregation.newAggregation(Key::class.java, Aggregation.group("user_id", "semester", "year"))
        reactiveMongoTemplate.aggregate(agg, "timetables", AggResult::class.java)
            .asFlow()
            .map { autoSetPrimary(it.id) }
            .rateLimiter(rateLimiter)
            .collect { }
    }

    private suspend fun autoSetPrimary(key: Key) {
        val (userId, semester, year) = key
        val timetables = timetableRepository.findAllByUserIdAndYearAndSemester(userId, year, semester).toList()
        if (timetables.any { it.isPrimary == true }) {
            log.info("SKIPPED $key | primary table alrady exists")
            return
        }

        val newPrimaryTimetable = timetables.maxBy { it.updatedAt }
        timetableRepository.save(newPrimaryTimetable.copy(isPrimary = true))
        log.info("UPDATED $key | primary table ID : ${newPrimaryTimetable.id}")
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        const val JOB_NAME = "primaryTimetableAutoSetJob"
        const val STEP_NAME = "primaryTimetableAutoSetStep"
    }
}
