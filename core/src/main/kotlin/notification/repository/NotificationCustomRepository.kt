package com.wafflestudio.snu4t.notification.repository

import com.wafflestudio.snu4t.notification.data.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues
import java.time.LocalDateTime

interface NotificationCustomRepository {
    fun findNotifications(userId: String, createdAt: LocalDateTime, offset: Long, limit: Int): Flow<Notification>
}

class NotificationRepositoryImpl(private val reactiveMongoTemplate: ReactiveMongoTemplate) : NotificationCustomRepository {
    override fun findNotifications(userId: String, createdAt: LocalDateTime, offset: Long, limit: Int) =
        reactiveMongoTemplate.find<Notification>(
            Query.query(
                Notification::userId.inValues(listOf(null, userId))
                    .and(Notification::createdAt).gt(createdAt)
            ).with(Sort.sort(Notification::createdAt.javaClass).descending()).skip(offset).limit(limit)
        ).asFlow()
}
