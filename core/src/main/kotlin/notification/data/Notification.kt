package com.wafflestudio.snutt.notification.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document("notifications")
@CompoundIndex(def = "{ 'user_id': 1, 'created_at': -1 }")
data class Notification(
    @Id
    val id: String? = null,
    /**
     * null 이면 모든 유저에게 보임
     */
    @Indexed
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    val userId: String?,
    val title: String,
    val message: String,
    val type: NotificationType,
    val deeplink: String?,
    @Indexed(direction = IndexDirection.DESCENDING)
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
