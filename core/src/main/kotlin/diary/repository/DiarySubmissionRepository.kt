package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiarySubmission
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DiarySubmissionRepository : CoroutineCrudRepository<DiarySubmission, String> {
    suspend fun findAllByUserIdOrderByCreatedAt(userId: String): List<DiarySubmission>

    suspend fun findAllByUserIdAndLectureIdOrderByCreatedAt(
        userId: String,
        lectureId: String,
    ): List<DiarySubmission>
}
