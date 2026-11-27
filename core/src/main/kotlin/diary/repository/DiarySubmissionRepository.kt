package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiarySubmission
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate
import java.time.LocalDateTime

interface DiarySubmissionRepository : CoroutineCrudRepository<DiarySubmission, String> {
    suspend fun findAllByUserIdOrderByCreatedAtDesc(userId: String): List<DiarySubmission>

    suspend fun findAllByClassDateIsNull(): List<DiarySubmission>

    suspend fun findAllByUserIdAndCreatedAtIsAfter(
        userId: String,
        createdAt: LocalDateTime,
    ): Set<DiarySubmission>

    suspend fun existsByUserIdAndLectureIdAndClassDate(
        userId: String,
        lectureId: String,
        classDate: LocalDate,
    ): Boolean
}
