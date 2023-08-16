package com.wafflestudio.snu4t.vacancynotification.repository

import com.wafflestudio.snu4t.vacancynotification.data.VacancyNotification
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface VacancyNotificationRepository : CoroutineCrudRepository<VacancyNotification, String> {
    fun findAllByLectureId(lectureId: String): Flow<VacancyNotification>
    fun findAllByUserId(userId: String): Flow<VacancyNotification>
    suspend fun existsByUserIdAndLectureId(userId: String, lectureId: String): Boolean
    suspend fun deleteByUserIdAndLectureId(userId: String, lectureId: String)
}
