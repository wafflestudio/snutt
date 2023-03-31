package com.wafflestudio.snu4t.notification.repository

import com.wafflestudio.snu4t.notification.data.Notification
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NotificationRepository : CoroutineCrudRepository<Notification, String> {

    suspend fun findAllByUserIdInAndCreatedAtGreaterThan(userIds: List<String?>, createdAt: LocalDateTime, pageable: Pageable): Flow<Notification>
    suspend fun countByUserIdInAndCreatedAtAfter(userIds: List<String?>, createdAt: LocalDateTime): Long
}

// 전체 공지도 포함해야 함 (전체공지는 userId = null)
// 따라서 in 절 쿼리를 사용하였음

suspend fun NotificationRepository.findNotifications(userId: String, createdAt: LocalDateTime, pageable: Pageable): Flow<Notification> {
    return findAllByUserIdInAndCreatedAtGreaterThan(listOf(userId, null), createdAt, pageable)
}

suspend fun NotificationRepository.countUnreadNotifications(userId: String, createdAt: LocalDateTime): Long {
    return countByUserIdInAndCreatedAtAfter(listOf(userId, null), createdAt)
