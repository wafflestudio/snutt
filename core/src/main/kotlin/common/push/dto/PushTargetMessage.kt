package common.push.dto

import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

data class PushTargetMessage(
    val targetToken: String,
    val title: String,
    val body: String,
) {
    fun toMessage(): Message {
        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build()

        return Message.builder()
            .setNotification(notification)
            .setToken(targetToken)
            .build()
    }
}
