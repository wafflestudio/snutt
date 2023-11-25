package com.wafflestudio.snu4t.notification.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.extension.toZonedDateTime
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.data.NotificationType
import java.time.ZonedDateTime

data class NotificationResponse(
    @JsonProperty("_id")
    val id: String? = null,
    @JsonProperty("user_id")
    val userId: String?,
    val title: String,
    val message: String,
    val type: NotificationType,
    @JsonProperty("url_scheme")
    val urlScheme: String?,
    @JsonProperty("created_at")
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id,
            userId = notification.userId,
            title = notification.title,
            message = notification.message,
            type = notification.type,
            urlScheme = notification.urlScheme,
            createdAt = notification.createdAt.toZonedDateTime(),
        )
    }
}
