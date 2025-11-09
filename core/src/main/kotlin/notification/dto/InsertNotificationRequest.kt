package notification.dto

import com.wafflestudio.snutt.notification.data.NotificationType

data class InsertNotificationRequest(
    /**
     * null 이면 모든 유저에게 보냄
     */
    val userId: String?,
    val title: String,
    val body: String,
    val insertFcm: Boolean,
    val shouldSendAsDataMessage: Boolean,
    val type: NotificationType = NotificationType.NORMAL,
    val dataPayload: Map<String, String> = emptyMap(),
)
