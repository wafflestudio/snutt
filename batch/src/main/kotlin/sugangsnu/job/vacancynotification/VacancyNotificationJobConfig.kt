package com.wafflestudio.snutt.sugangsnu.job.vacancynotification

import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.sugangsnu.common.service.SugangSnuNotificationService
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.data.VacancyNotificationJobResult
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.service.VacancyNotifierService
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
import java.time.Instant
import java.time.ZoneId

@Configuration
class VacancyNotificationJobConfig {
    @Bean
    fun vacancyNotificationJob(
        jobRepository: JobRepository,
        vacancyNotificationStep: Step,
    ): Job =
        JobBuilder("vacancyNotificationJob", jobRepository)
            .start(vacancyNotificationStep)
            .build()

    @Bean
    fun vacancyNotificationStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        vacancyNotifierService: VacancyNotifierService,
        coursebookService: CoursebookService,
        sugangSnuNotificationService: SugangSnuNotificationService,
    ): Step =
        StepBuilder("vacancyNotificationStep", jobRepository).tasklet(
            { _, _ ->
                runBlocking {
                    val latestCoursebook = coursebookService.getLatestCoursebook()
                    val updateResult = vacancyNotifierService.noti(latestCoursebook)
                    if (Instant.now().atZone(ZoneId.of("Asia/Seoul")).hour == 18) return@runBlocking RepeatStatus.FINISHED
                    when (updateResult) {
                        VacancyNotificationJobResult.REGISTRATION_IS_NOT_STARTED -> RepeatStatus.FINISHED
                        VacancyNotificationJobResult.OVERLOAD_PERIOD -> RepeatStatus.CONTINUABLE
                        VacancyNotificationJobResult.SUCCESS -> RepeatStatus.CONTINUABLE
                    }
                }
            },
            transactionManager,
        ).build()
}
