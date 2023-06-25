package com.wafflestudio.snu4t.notification.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.data.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    @JsonProperty("_id")
    val id: String? = null,
    @JsonProperty("user_id")
    val userId: String?,
    val title: String,
    val body: String,
    val message: String,
    val type: NotificationType,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id,
            userId = notification.userId,
            title = notification.title,
            body = notification.body,
            message = notification.message,
            type = notification.type,
            createdAt = notification.createdAt,
        )
    }
}
