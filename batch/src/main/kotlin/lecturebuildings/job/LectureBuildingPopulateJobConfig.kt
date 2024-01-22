package com.wafflestudio.snu4t.lecturebuildings.job

import com.wafflestudio.snu4t.lecturebuildings.service.LectureBuildingPopulateService
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.service.LectureService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
class LectureBuildingPopulateJobConfig(
    private val lectureService: LectureService,
    private val lectureBuildingPopulateService: LectureBuildingPopulateService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val JOB_NAME = "lectureBuildingPopulateJob"
        const val STEP_NAME = "lectureBuildingPopulateStep"
    }

    @Bean
    fun lectureBuildingCreateJob(jobRepository: JobRepository, lectureBuildingCreateStep: Step): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(lectureBuildingCreateStep)
            .build()
    }

    @Bean
    fun lectureBuildingCreateStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager
    ): Step = StepBuilder(STEP_NAME, jobRepository).tasklet(
        { _, _ ->
            runBlocking {
                val lectures = lectureService.findAll()
                    .onEach {
                        tryToUpdateLectureBuilding(it)
                    }
                    .collect()
            }
            RepeatStatus.FINISHED
        },
        transactionManager
    ).build()

    private suspend fun tryToUpdateLectureBuilding(lecture: Lecture) = runBlocking {
        val updateResult = lectureBuildingPopulateService.populateLectureBuildings(lecture)

        if (updateResult.buildingsAdded.isEmpty()) {
            if (lecture.classPlaceAndTimes.map { it.place }.isEmpty()) {
                log.info("강의(id: #${lecture.id}, title: ${lecture.courseTitle})의 강의동 데이터가 없습니다.")
            } else {
                log.error("강의(id: #${lecture.id}, title: ${lecture.courseTitle})의 강의동 데이터 데이터를 불러오는데 실패했습니다..")
            }
        } else {
            log.info(
                "강의(id: #${lecture.id}, title: ${lecture.courseTitle}의 강의동(${
                updateResult.buildingsAdded.map { "${it.buildingNameKor}(${it.buildingNumber})" }.joinToString(", ")
                })을 추가합니다."
            )
        }
    }
}
