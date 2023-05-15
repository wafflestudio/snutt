package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("test")
@Component
class LectureFixture(private val lectureRepository: LectureRepository) {
    suspend fun saveTestLecture() {
        lectureRepository.save(
            Lecture(
                academicYear = "1",
                category = "",
                classPlaceAndTimes = listOf(),
                classTimeMask = listOf(),
                classification = "",
                credit = 1,
                department = "",
                instructor = "",
                lectureNumber = "",
                quota = 100,
                remark = "",
                semester = Semester.AUTUMN,
                year = 2021,
                courseNumber = "",
                courseTitle = "",
                registrationCount = 0
            )
        )
    }
}
