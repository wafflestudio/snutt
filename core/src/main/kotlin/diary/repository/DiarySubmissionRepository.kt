package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.diary.data.DiarySubmission
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DiarySubmissionRepository : CoroutineCrudRepository<DiarySubmission, String> {
    suspend fun findAllByUserIdAndYearAndSemesterOrderByCreatedAtDesc(
        userId: String,
        year: Int,
        semester: Semester,
    ): List<DiarySubmission>

    suspend fun findAllByUserIdAndLectureIdOrderByCreatedAtDesc(
        userId: String,
        lectureId: String,
    ): List<DiarySubmission>
}
