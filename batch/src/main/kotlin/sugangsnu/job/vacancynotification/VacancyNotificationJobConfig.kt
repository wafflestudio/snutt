package com.wafflestudio.snutt.sugangsnu.job.vacancynotification

import com.wafflestudio.snutt.common.JobFailureLoggingListener
import com.wafflestudio.snutt.common.exception.RegistrationPeriodNotSetException
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.registrationperiod.service.SemesterRegistrationPeriodService
import com.wafflestudio.snutt.sugangsnu.common.service.SugangSnuNotificationService
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.data.VacancyNotificationJobResult
import com.wafflestudio.snutt.sugangsnu.job.vacancynotification.service.VacancyNotifierService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
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
        jobFailureLoggingListener: JobFailureLoggingListener,
        vacancyNotificationStep: Step,
    ): Job =
        JobBuilder("vacancyNotificationJob", jobRepository)
            .listener(jobFailureLoggingListener)
            .start(vacancyNotificationStep)
            .build()

    @Bean
    fun vacancyNotificationStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        vacancyNotifierService: VacancyNotifierService,
        coursebookService: CoursebookService,
        sugangSnuNotificationService: SugangSnuNotificationService,
        semesterRegistrationPeriodService: SemesterRegistrationPeriodService,
    ): Step =
        StepBuilder("vacancyNotificationStep", jobRepository)
            .tasklet(
                { _, _ ->
                    runBlocking {
                        val latestCoursebook = coursebookService.getLatestCoursebook()
                        val registrationPeriods =
                            semesterRegistrationPeriodService
                                .getByYearAndSemester(latestCoursebook.year, latestCoursebook.semester)
                                ?.registrationPeriods ?: throw RegistrationPeriodNotSetException
                        if (Instant.now().atZone(KST).hour >= 18) return@runBlocking RepeatStatus.FINISHED
                        val currentDate = Instant.now().atZone(KST).toLocalDate()
                        val registrationDay =
                            registrationPeriods.find {
                                currentDate == it.date
                            } ?: return@runBlocking RepeatStatus.FINISHED
                        val updateResult =
                            vacancyNotifierService.notify(
                                latestCoursebook,
                                registrationDay.phase,
                                registrationDay.vacantSeatRegistrationTimes,
                            )
                        when (updateResult) {
                            VacancyNotificationJobResult.REGISTRATION_IS_NOT_STARTED -> RepeatStatus.FINISHED
                            VacancyNotificationJobResult.OVERLOAD_PERIOD -> RepeatStatus.CONTINUABLE
                            VacancyNotificationJobResult.SUCCESS -> RepeatStatus.CONTINUABLE
                        }
                    }
                },
                transactionManager,
            ).build()

    companion object {
        private val KST = ZoneId.of("Asia/Seoul")
    }
}
