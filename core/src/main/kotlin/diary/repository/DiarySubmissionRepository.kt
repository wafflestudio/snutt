package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiarySubmission
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

interface DiarySubmissionRepository : CoroutineCrudRepository<DiarySubmission, String> {
    suspend fun findAllByUserIdOrderByCreatedAtDesc(userId: String): List<DiarySubmission>

    suspend fun findAllByUserIdAndLectureIdAndCreatedAtAfterOrderByCreatedAtDesc(
        userId: String,
        lectureId: String,
        createdAt: Instant,
    ): List<DiarySubmission>
}
