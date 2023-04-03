package notification.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.notification.data.NotificationType

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class InsertNotificationRequest(
    val userId: String? = null,
    val title: String,
    val body: String,
    val insertFcm: Boolean = false,
    val type: NotificationType = NotificationType.NORMAL,
    val dataPayload: Map<String, String> = emptyMap()
)
