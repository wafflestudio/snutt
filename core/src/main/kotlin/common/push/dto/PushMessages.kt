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
 * 푸시 메시지로 전송할 데이터
 */
data class PushMessage(
    val title: String,
    val body: String,
    val data: Data = Data(emptyMap()),
) {
    data class Data(val payload: Map<String, String>)

    fun toNotification(notificationType: NotificationType, userId: String?): Notification {
        return Notification(
            userId = userId,
            title = title,
            message = body,
            type = notificationType,
        )
    }
}

fun PushMessage(
    title: String,
    body: String,
    data: Map<String, String>,
) = PushMessage(title, body, PushMessage.Data(data))

/**
 * Keys used in Push Message Data
 */
private object Keys {
    const val URL_SCHEME = "url_scheme"
}

fun PushMessage(
    title: String,
    body: String,
    urlScheme: UrlScheme.Compiled,
): PushMessage {
    val data = mapOf(Keys.URL_SCHEME to urlScheme.value)
    return PushMessage(title, body, PushMessage.Data(data))
}
