package com.wafflestudio.snu4t.notification.repository

import com.wafflestudio.snu4t.notification.data.Notification
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NotificationRepository : CoroutineCrudRepository<Notification, String> {

    suspend fun findAllByUserIdAndCreatedAtGreaterThan(userId: String, createdAt: LocalDateTime, pageable: Pageable): Flow<Notification>
    suspend fun countByUserIdAndCreatedAtAfter(userId: String, createdAt: LocalDateTime): Long
}
