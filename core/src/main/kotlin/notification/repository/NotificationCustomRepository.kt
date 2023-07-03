package com.wafflestudio.snu4t.notification.repository

import com.wafflestudio.snu4t.common.desc
import com.wafflestudio.snu4t.notification.data.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.gt
import org.springframework.data.mongodb.core.query.inValues
import java.time.LocalDateTime

interface NotificationCustomRepository {
    fun findNotifications(userId: String, createdAt: LocalDateTime, offset: Long, limit: Int): Flow<Notification>
}

class NotificationRepositoryImpl(private val reactiveMongoTemplate: ReactiveMongoTemplate) : NotificationCustomRepository {
    override fun findNotifications(userId: String, createdAt: LocalDateTime, offset: Long, limit: Int) =
        reactiveMongoTemplate.find<Notification>(
            Query.query(
                Criteria().andOperator(
                    Notification::createdAt gt createdAt,
                    Notification::userId inValues listOf(userId, null),
                )
            ).apply {
                with(Notification::createdAt.desc())
                skip(offset)
                limit(limit)
            }
        ).asFlow()
}
