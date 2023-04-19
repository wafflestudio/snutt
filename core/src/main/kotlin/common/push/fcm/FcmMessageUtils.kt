package com.wafflestudio.snu4t.common.push.fcm

import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.common.push.dto.TopicMessage

internal fun TopicMessage.toFcmMessage(): Message {
    val notification = Notification.builder()
        .setTitle(message.title)
        .setBody(message.body)
        .build()

    return Message.builder()
        .setNotification(notification)
        .setTopic(topic)
        .putAllData(message.data.payload)
        .build()
}

internal fun PushTargetMessage.toFcmMessage(): Message {
    val notification = Notification.builder()
        .setTitle(message.title)
        .setBody(message.body)
        .build()

    return Message.builder()
        .setNotification(notification)
        .setToken(targetToken)
        .putAllData(message.data.payload)
        .build()
}
