package com.wafflestudio.snutt.notification.repository

import com.wafflestudio.snutt.notification.data.Notification
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NotificationRepository :
    CoroutineCrudRepository<Notification, String>,
    NotificationCustomRepository {
    suspend fun countByUserIdInAndCreatedAtAfter(
        userIds: List<String?>,
        createdAt: LocalDateTime,
    ): Long
}

suspend fun NotificationRepository.countUnreadNotifications(
    userId: String,
    createdAt: LocalDateTime,
): Long = countByUserIdInAndCreatedAtAfter(listOf(userId, null), createdAt)
