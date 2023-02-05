package com.wafflestudio.snu4t.sugangsnu

import com.wafflestudio.snu4t.lectures.service.LectureService
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
class SugangSnuFetchJobConfig {
    @Bean
    fun sugangSnuMigrationJob(jobRepository: JobRepository, fetchSugangSnuStep: Step): Job =
        JobBuilder("sugangSnuMigrationJob", jobRepository)
            .start(fetchSugangSnuStep).build()

    @Bean
    fun fetchSugangSnuStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        sugangSnuFetchService: SugangSnuFetchService,
        lectureService: LectureService,
    ): Step = StepBuilder("fetchSugangSnuStep", jobRepository).tasklet(
        { _, _ ->
            runBlocking {

                val coursebook = sugangSnuFetchService.getOrCreateLatestCoursebook()
                val newLectures = sugangSnuFetchService.getLectures(coursebook.year, coursebook.semester)
                val oldLectures =
                    lectureService.getLecturesByYearAndSemesterAsFlow(coursebook.year, coursebook.semester).toList()
                val compareResult = sugangSnuFetchService.compareLectures(newLectures, oldLectures)

                sugangSnuFetchService.syncLectures(compareResult)
            }
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()

    @Bean
    fun userLectureIdChange(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        sugangSnuFetchService: SugangSnuFetchService,
    ): Step = StepBuilder("fetchSugangSnuStep", jobRepository).tasklet(
        { _, _ ->
            runBlocking {
                sugangSnuFetchService.userLectureIdChange()
            }
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()
}
