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
class SugangSnuMigrationJobConfig {
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

//    @Bean
//    fun sugangSnuFetch(
//        jobRepository: JobRepository,
//        transactionManager: PlatformTransactionManager
//    ): Step = StepBuilder("step1", jobRepository).tasklet({ _, _ ->
//
//    }, transactionManager).build()
////
//    fun step3(): Step = StepBuilder("step3", jobRepository).tasklet({ _, _ ->
//        RepeatStatus.FINISHED
//    }, transactionManger).build()
//
//
//    fun checkNewCoursebook(): Step = StepBuilder("checkNewCoursebook", jobRepository)
//        .tasklet({_,_ -> RepeatStatus.FINISHED}, transactionManger)
//        .build()

//    fun sugangSnuFetchingStep(): Step = StepBuilder("sugangSnuFetchingStep", jobRepository)
//        .chunk<Coursebook, Coursebook>(100, transactionManger)
//        .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManger)
//        .chunk<UgcNaviUuid, UgcNaviUuid>(CHUNK_SIZE, transactionManger)
//        .processor()
//        .reader(reader())
//        .writer(writer()).build()

//    @Bean
//    @StepScope
//    fun reader(): FlatFileItemReader<TimeTable> {
//        return FlatFileItemReaderBuilder<TimeTable>()
//            .resource(F)
//            .build()
//    }
//
//
//    @Bean
//    fun writer(): ItemWriter<UgcNaviUuid> {
//        return ItemWriter { items ->
//            runBlocking {
//                val naverIdIdNoMap =
//                    userProfileService.getIdNosByNaverIds(items.map { it.naverId }).associate { it.naverId to it.idNo }
//                try {
//                    userNaviUuidRepository.saveAllAtOnce(items.map {
//                        UserNaviUuid(naverIdIdNoMap[it.naverId]!!, it.uuid, it.regTime)
//                    })
//                } catch (_: MongoBulkWriteException) {
//                }
//            }
//        }
//    }
}
