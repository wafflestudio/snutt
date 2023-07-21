package notification.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.notification.data.NotificationType

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class InsertNotificationRequest(
    /**
     * null 이면 모든 유저에게 보냄
     */
    val userId: String?,
    val title: String,
    val body: String,
    val insertFcm: Boolean,
    val type: NotificationType = NotificationType.NORMAL,
    val dataPayload: Map<String, String> = emptyMap()
)
