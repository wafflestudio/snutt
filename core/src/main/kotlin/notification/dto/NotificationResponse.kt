package com.wafflestudio.snutt.notification.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.extension.toZonedDateTime
import com.wafflestudio.snutt.notification.data.Notification
import com.wafflestudio.snutt.notification.data.NotificationType
import java.time.ZonedDateTime

data class NotificationResponse(
    @param:JsonProperty("_id")
    val id: String? = null,
    @param:JsonProperty("user_id")
    val userId: String?,
    val title: String,
    val message: String,
    val type: NotificationType,
    val deeplink: String?,
    @param:JsonProperty("created_at")
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(notification: Notification) =
            NotificationResponse(
                id = notification.id,
                userId = notification.userId,
                title = notification.title,
                message = notification.message,
                type = notification.type,
                deeplink = notification.deeplink,
                createdAt = notification.createdAt.toZonedDateTime(),
            )
    }
}
