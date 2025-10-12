package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiarySubmission
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime

interface DiarySubmissionRepository : CoroutineCrudRepository<DiarySubmission, String> {
    suspend fun findAllByUserIdOrderByCreatedAtDesc(userId: String): List<DiarySubmission>

    suspend fun findAllByUserIdAndCreatedAtIsAfter(
        userId: String,
        createdAt: LocalDateTime,
    ): Set<DiarySubmission>
}
