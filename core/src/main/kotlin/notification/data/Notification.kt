package com.wafflestudio.snu4t.notification.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document("notifications")
data class Notification(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    @JsonProperty("user_id")
    val userId: String?,
    val message: String,
    val type: NotificationType,
    val link: String? = null,
    @Indexed
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
