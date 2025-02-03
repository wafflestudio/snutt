package com.wafflestudio.snutt.common.push.dto

import com.wafflestudio.snutt.common.push.DeeplinkType
import com.wafflestudio.snutt.notification.data.Notification
import com.wafflestudio.snutt.notification.data.NotificationType

sealed class TargetedPushMessage(
    open val message: PushMessage,
)

/**
 * Send Message By Target Push Token
 */
data class TargetedPushMessageWithToken(
    val targetToken: String,
    override val message: PushMessage,
) : TargetedPushMessage(message)

/**
 * Send Message By FCM Topic
 */
data class TargetedPushMessageWithTopic(
    val topic: String,
    override val message: PushMessage,
) : TargetedPushMessage(message)

/**
 * 푸시 메시지로 전송할 데이터
 */
data class PushMessage(
    val title: String,
    val body: String,
    val urlScheme: DeeplinkType? = null,
    val data: Data = Data(emptyMap()),
) {
    data class Data(val payload: Map<String, String>)

    fun toNotification(
        notificationType: NotificationType,
        userId: String?,
    ): Notification {
        return Notification(
            userId = userId,
            title = title,
            message = body,
            type = notificationType,
            deeplink = urlScheme?.build()?.value,
        )
    }
}

fun PushMessage(
    title: String,
    body: String,
    data: Map<String, String>,
) = PushMessage(title, body, data = PushMessage.Data(data))
