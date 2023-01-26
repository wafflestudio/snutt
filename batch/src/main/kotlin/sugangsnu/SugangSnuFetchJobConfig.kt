package com.wafflestudio.snu4t.sugangsnu

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
        sugangSnuService: SugangSnuFetchService,
    ): Step = StepBuilder("fetchSugangSnuStep", jobRepository).tasklet({ _, _ ->
        runBlocking {
            val coursebook = sugangSnuService.getOrCreateLatestCoursebook()
            sugangSnuService.getLectures(coursebook.year, coursebook.semester)

            RepeatStatus.FINISHED
        }
    }, transactionManager).build()
}
