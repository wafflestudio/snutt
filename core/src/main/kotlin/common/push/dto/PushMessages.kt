package com.wafflestudio.snu4t.common.push.dto

import com.wafflestudio.snu4t.common.push.UrlScheme
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.data.NotificationType

/**
 * Send Message By Target Push Token
 */
data class PushTargetMessage(
    val targetToken: String,
    val message: PushMessage
)

/**
 * Send Message By FCM Topic
 */
data class TopicMessage(
    val topic: String,
    val message: PushMessage
)

/**
 * Message To Send
 */
data class PushMessage(
    val title: String,
    val body: String,
    val type: NotificationType,
    val data: Data = Data(emptyMap()),
    val detailMessage: String? = null,
) {
    data class Data(val payload: Map<String, String>)

    fun toNotification(userId: String?): Notification {
        return Notification(
            userId = userId,
            title = title,
            body = body,
            message = detailMessage ?: body,
            type = type,
        )
    }
}

fun PushMessage(
    title: String,
    body: String,
    type: NotificationType,
    data: Map<String, String>,
    detailMessage: String? = null,
) = PushMessage(title, body, type, PushMessage.Data(data), detailMessage)

/**
 * Keys used in Push Message Data
 */
private object Keys {
    const val URL_SCHEME = "url_scheme"
}

fun PushMessage(
    title: String,
    body: String,
    type: NotificationType,
    urlScheme: UrlScheme.Compiled,
    detailMessage: String? = null,
): PushMessage {
    val data = mapOf(Keys.URL_SCHEME to urlScheme.value)
    return PushMessage(title, body, type, PushMessage.Data(data), detailMessage)
}
