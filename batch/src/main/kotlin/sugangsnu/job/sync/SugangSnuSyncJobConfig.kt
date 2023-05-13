package com.wafflestudio.snu4t.sugangsnu.job.sync

import com.wafflestudio.snu4t.coursebook.service.CoursebookService
import com.wafflestudio.snu4t.sugangsnu.common.service.SugangSnuNotificationService
import com.wafflestudio.snu4t.sugangsnu.common.utils.nextCoursebook
import com.wafflestudio.snu4t.sugangsnu.job.sync.service.SugangSnuSyncService
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

@Configuration
class SugangSnuSyncJobConfig {
    @Bean
    fun sugangSnuMigrationJob(jobRepository: JobRepository, syncSugangSnuStep: Step): Job =
        JobBuilder("sugangSnuMigrationJob", jobRepository)
            .start(syncSugangSnuStep).build()

    @Bean
    fun syncSugangSnuStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        sugangSnuSyncService: SugangSnuSyncService,
        coursebookService: CoursebookService,
        sugangSnuNotificationService: SugangSnuNotificationService,
    ): Step = StepBuilder("fetchSugangSnuStep", jobRepository).tasklet(
        { _, _ ->
            runBlocking {
                val existingCoursebook = coursebookService.getLatestCoursebook()
                if (sugangSnuSyncService.isSyncWithSugangSnu(existingCoursebook)) {
                    val updateResult = sugangSnuSyncService.updateCoursebook(existingCoursebook)
                    sugangSnuNotificationService.notifyUserLectureChanges(updateResult)
                } else {
                    val newCoursebook = existingCoursebook.nextCoursebook()
                    sugangSnuSyncService.addCoursebook(newCoursebook)
                    sugangSnuNotificationService.notifyCoursebookUpdate(newCoursebook)
                }
                sugangSnuSyncService.flushCache()
            }
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()
}
