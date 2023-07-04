package com.wafflestudio.snu4t.users.job

import com.wafflestudio.snu4t.common.extension.isEqualTo
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.service.UserNicknameGenerateService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
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
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@Configuration
class UserNicknameCreateJobConfig(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val userNicknameGenerateService: UserNicknameGenerateService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val JOB_NAME = "userNicknameCreateJob"
        const val STEP_NAME = "userNicknameCreateStep"
    }

    @Bean
    fun userNicknameCreateJob(jobRepository: JobRepository, userNicknameCreateStep: Step): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(userNicknameCreateStep)
            .build()
    }

    @Bean
    @OptIn(ExperimentalTime::class)
    fun userNicknameCreateStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step = StepBuilder(STEP_NAME, jobRepository).tasklet(
        { _, _ ->
            val (updateCount, elapsedTime) = measureTimedValue { updateUserNickname() }
            log.info("닉네임 생성 작업이 완료되었습니다. 총 ${updateCount}명 유저의 닉네임을 생성했습니다. (소요시간: ${elapsedTime.inWholeSeconds}초)")
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()

    private fun updateUserNickname(): Int = runBlocking {
        val updateCount = AtomicInteger()
        val channel = Channel<User>(capacity = 100)

        repeat(100) {
            launch {
                for (user in channel) {
                    tryToUpdateNickname(user)
                    updateCount.incrementAndGet()
                }
            }
        }

        reactiveMongoTemplate.find<User>(Query.query(User::nickname isEqualTo null))
            .asFlow()
            .collect { channel.send(it) }

        channel.close()
        updateCount.get()
    }

    private suspend fun tryToUpdateNickname(user: User) {
        val nickname = userNicknameGenerateService.generateRandomNickname()
        val updated = user.copy(nickname = nickname)
        try {
            reactiveMongoTemplate.save(updated).awaitSingle()
            log.info("유저(id: #${user.id}, email:#${user.email})의 닉네임을 ${updated.nickname}로 변경했습니다.")
        } catch (e: DuplicateKeyException) {
            log.info("유저(id: #${user.id}, email:#${user.email})의 닉네임 ${user.nickname}이 중복되어 재시도합니다.")
            val unique = user.copy(nickname = userNicknameGenerateService.generateUniqueRandomNickname())
            reactiveMongoTemplate.save(unique).awaitSingle()
            log.info("유저(id: #${user.id}, email:#${user.email})의 닉네임을 ${unique.nickname}로 변경했습니다.")
        } catch (e: Exception) {
            log.error("유저(id: #${user.id}, email:#${user.email})의 닉네임 ${user.nickname} 변경에 실패했습니다.")
        }
    }
}
