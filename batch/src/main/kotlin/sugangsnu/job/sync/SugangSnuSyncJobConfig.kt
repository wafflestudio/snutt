package com.wafflestudio.snutt.sugangsnu.job.sync

import com.wafflestudio.snutt.common.JobFailureLoggingListener
import com.wafflestudio.snutt.coursebook.service.CoursebookService
import com.wafflestudio.snutt.sugangsnu.common.service.SugangSnuNotificationService
import com.wafflestudio.snutt.sugangsnu.common.utils.nextCoursebook
import com.wafflestudio.snutt.sugangsnu.job.sync.service.SugangSnuSyncService
import com.wafflestudio.snutt.vacancynotification.service.VacancyNotificationService
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

@Configuration
class SugangSnuSyncJobConfig {
    @Bean
    fun sugangSnuMigrationJob(
        jobRepository: JobRepository,
        jobFailureLoggingListener: JobFailureLoggingListener,
        syncSugangSnuStep: Step,
    ): Job =
        JobBuilder("sugangSnuMigrationJob", jobRepository)
            .listener(jobFailureLoggingListener)
            .start(syncSugangSnuStep)
            .build()

    @Bean
    fun syncSugangSnuStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        sugangSnuSyncService: SugangSnuSyncService,
        coursebookService: CoursebookService,
        sugangSnuNotificationService: SugangSnuNotificationService,
        vacancyNotificationService: VacancyNotificationService,
    ): Step =
        StepBuilder("fetchSugangSnuStep", jobRepository)
            .tasklet(
                { _, _ ->
                    runBlocking {
                        val existingCoursebook = coursebookService.getLatestCoursebook()
                        if (sugangSnuSyncService.isSyncWithSugangSnu(existingCoursebook)) {
                            val updateResult = sugangSnuSyncService.updateCoursebook(existingCoursebook)
                            sugangSnuNotificationService.notifyUserLectureChanges(updateResult)
                        } else {
                            val newCoursebook = existingCoursebook.nextCoursebook()
                            vacancyNotificationService.deleteAll()
                            sugangSnuSyncService.addCoursebook(newCoursebook)
                            sugangSnuNotificationService.notifyCoursebookUpdate(newCoursebook)
                        }
                    }
                    RepeatStatus.FINISHED
                },
                transactionManager,
            ).build()
}
