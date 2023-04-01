package common.push.dto

import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

data class TopicMessage(val topic: String, val payload: MessagePayload) {
    fun toMessage(): Message {
        val notification = Notification.builder()
            .setTitle(payload.title)
            .setBody(payload.body)
            .build()

        return Message.builder()
            .setNotification(notification)
            .setTopic(topic)
            .setUrlScheme(payload.urlSchemeString)
            .build()
    }
}

data class PushTargetMessage(val targetToken: String, val payload: MessagePayload) {
    fun toMessage(): Message {
        val notification = Notification.builder()
            .setTitle(payload.title)
            .setBody(payload.body)
            .build()

        return Message.builder()
            .setNotification(notification)
            .setToken(targetToken)
            .setUrlScheme(payload.urlSchemeString)
            .build()
    }
}

private const val URL_SCHEME = "urlScheme"

private fun Message.Builder.setUrlScheme(scheme: String): Message.Builder {
    if (scheme.isBlank()) {
        return this
    }

    putData(URL_SCHEME, scheme)
    return this
}
