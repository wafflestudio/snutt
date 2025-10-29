package com.wafflestudio.snutt.common.push.dto

import com.wafflestudio.snutt.common.push.Deeplink
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
    val urlScheme: Deeplink? = null,
    val data: Data = Data(emptyMap()),
    /**
     * true라면 안드로이드 doze 모드(배터리 절약 모드) 중에
     * 기기를 깨우고 정확한 알림을 보낸다
     */
    val isUrgentOnAndroid: Boolean = false,
    /**
     * true라면 data message로 보내고, false라면 notification message로 보낸다.
     * data message는 클라이언트에서 직접
     * @see [Firebase 문서](https://firebase.google.com/docs/cloud-messaging/customize-messages/set-message-type)
     */
    val shouldSendAsDataMessage: Boolean = false,
) {
    data class Data(
        val payload: Map<String, String>,
    )

    fun toNotification(
        notificationType: NotificationType,
        userId: String?,
    ): Notification =
        Notification(
            userId = userId,
            title = title,
            message = body,
            type = notificationType,
            deeplink = urlScheme?.value,
        )
}

fun PushMessage(
    title: String,
    body: String,
    data: Map<String, String>,
) = PushMessage(title, body, data = PushMessage.Data(data))
