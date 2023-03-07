package com.wafflestudio.snu4t.sugangsnu

import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.sugangsnu.service.SugangSnuFetchService
import com.wafflestudio.snu4t.sugangsnu.service.SugangSnuNotificationService
import com.wafflestudio.snu4t.sugangsnu.service.SugangSnuSyncService
import kotlinx.coroutines.flow.toList
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
    fun sugangSnuMigrationJob(jobRepository: JobRepository, fetchSugangSnuStep: Step): Job =
        JobBuilder("sugangSnuMigrationJob", jobRepository)
            .start(fetchSugangSnuStep).build()

    @Bean
    fun fetchSugangSnuStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        sugangSnuFetchService: SugangSnuFetchService,
        sugangSnuSyncService: SugangSnuSyncService,
        sugangSnuNotificationService: SugangSnuNotificationService,
        lectureService: LectureService,
    ): Step = StepBuilder("fetchSugangSnuStep", jobRepository).tasklet(
        { _, _ ->
            runBlocking {
                val coursebook = sugangSnuSyncService.getOrCreateLatestCoursebook()
                val newLectures = sugangSnuFetchService.getLectures(coursebook.year, coursebook.semester)
                val oldLectures =
                    lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
                val compareResult = sugangSnuSyncService.compareLectures(newLectures, oldLectures)

                sugangSnuSyncService.syncLectures(compareResult)
                val syncUserLecturesResults = sugangSnuSyncService.syncSavedLectures(compareResult)
                sugangSnuNotificationService.notifyUserLectureChanges(syncUserLecturesResults)
            }
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()
}
