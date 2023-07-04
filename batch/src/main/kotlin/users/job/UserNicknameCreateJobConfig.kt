package com.wafflestudio.snu4t.users.job

import com.wafflestudio.snu4t.common.isEqualTo
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.service.UserNicknameGenerateService
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
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
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class UserNicknameCreateJobConfig(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val userNicknameGenerateService: UserNicknameGenerateService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val JOB_NAME = "userNicknameCreateJob"
        const val STEP_NAME = "userNicknameCreateStep"
        private const val BATCH_SIZE = 2000
    }

    @Bean
    fun userNicknameCreateJob(jobRepository: JobRepository, userNicknameCreateStep: Step): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(userNicknameCreateStep)
            .build()
    }

    @Bean
    fun userNicknameCreateStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step = StepBuilder(STEP_NAME, jobRepository).tasklet(
        { _, _ ->
            runBlocking {
                var updateCount: Int
                do { updateCount = updateUserNickname() }
                while (updateCount > 0)
            }
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()

    private suspend fun updateUserNickname(): Int {
        return reactiveMongoTemplate.find<User>(Query.query(User::nickname isEqualTo null).limit(BATCH_SIZE))
            .asFlow()
            .map { tryToUpdateNickname(it) }
            .retryWhen { cause, attempt ->
                log.info("닉네임 충돌 발생! ${attempt}번째 재시도 중 ($cause)")
                cause is DuplicateKeyException && attempt < 100
            }
            .count()
    }

    private suspend fun tryToUpdateNickname(user: User) {
        val nickname = userNicknameGenerateService.generateRandomNickname()
        val updated = user.copy(nickname = nickname)
        reactiveMongoTemplate.save(updated).block()
        log.info("유저(id: #${user.id}, email:#${user.email})의 닉네임을 ${updated.nickname}로 변경했습니다.")
    }
}
