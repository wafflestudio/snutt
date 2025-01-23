package com.wafflestudio.snu4t.oldcategory.job

import com.wafflestudio.snu4t.oldcategory.service.OldCategoryFetchService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class AddOldCategoryJobConfig {
    companion object {
        const val JOB_NAME = "addOldCategoryJob"
        const val STEP_NAME = "addOldCategoryStep"
    }

    @Bean
    fun addOldCategoryJob(
        jobRepository: JobRepository,
        addOldCategoryStep: Step,
        ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(addOldCategoryStep)
            .build()
    }

    @Bean
    @JobScope
    fun addOldCategoryStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        oldCategoryFetchService: OldCategoryFetchService,
    ): Step =
        StepBuilder(STEP_NAME, jobRepository)
            .tasklet(
                { _, _ ->
                    runBlocking {
                        oldCategoryFetchService.applyOldCategories()
                    }
                    RepeatStatus.FINISHED
                },
                transactionManager
            )
            .build()
}
