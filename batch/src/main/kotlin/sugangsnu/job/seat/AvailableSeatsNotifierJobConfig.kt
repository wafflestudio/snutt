package com.wafflestudio.snu4t.sugangsnu.job.seat

import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.sugangsnu.common.service.SugangSnuNotificationService
import com.wafflestudio.snu4t.sugangsnu.job.seat.data.AvailableSeatsNotificationResult
import com.wafflestudio.snu4t.sugangsnu.job.seat.service.AvailableSeatsNotifierService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Configuration
class AvailableSeatsNotifierJobConfig {
    @Bean
    fun availableSeatsNotifier(jobRepository: JobRepository, availableSeatsNotifierStep: Step): Job =
        JobBuilder("availableSeatsNotifier", jobRepository)
            .start(availableSeatsNotifierStep)
            .build()

    @Bean
    fun availableSeatsNotifierStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        availableSeatsNotifierService: AvailableSeatsNotifierService,
        coursebookService: CoursebookService,
        sugangSnuNotificationService: SugangSnuNotificationService,
    ): Step = StepBuilder("availableSeatsNotifierStep", jobRepository).tasklet(
        { _, _ ->
            runBlocking {
                val latestCoursebook = coursebookService.getLatestCoursebook()
                val updateResult = availableSeatsNotifierService.noti(latestCoursebook)
                if(Instant.now().atZone(ZoneId.of("Asia/Seoul")).hour == 18) return@runBlocking RepeatStatus.FINISHED
                delay(30000)
                when(updateResult) {
                    AvailableSeatsNotificationResult.REGISTRATION_IS_NOT_STARTED -> RepeatStatus.FINISHED
                    AvailableSeatsNotificationResult.OVERLOAD_PERIOD -> RepeatStatus.CONTINUABLE
                    AvailableSeatsNotificationResult.SUCCESS -> RepeatStatus.CONTINUABLE
                }
            }
        },
        transactionManager
    ).build()
}
