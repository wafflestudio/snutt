package com.wafflestudio.snu4t.common.push.fcm

import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.common.push.dto.TopicMessage

internal fun TopicMessage.toFcmMessage(): Message {
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

internal fun PushTargetMessage.toFcmMessage(): Message {
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

private const val URL_SCHEME = "urlScheme"

private fun Message.Builder.setUrlScheme(scheme: String): Message.Builder {
    if (scheme.isBlank()) {
        return this
    }

    putData(URL_SCHEME, scheme)
    return this
}
