package com.wafflestudio.snu4t.common.push

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.wafflestudio.snu4t.common.push.data.FcmLog
import common.push.dto.MessageReason
import common.push.dto.PushTargetMessage
import common.push.dto.TopicMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface PushNotificationService {
    suspend fun sendMessage(pushMessage: PushTargetMessage, reason: MessageReason)
    suspend fun sendMessages(pushMessages: List<PushTargetMessage>, reason: MessageReason)
    suspend fun sendGlobalMessage(title: String, body: String, reason: MessageReason)
    suspend fun sendTopicMessage(pushMessage: TopicMessage, reason: MessageReason)
}

@Service
@Profile("!test")
class FcmPushNotificationService(
    @Value("\${google.firebase.project-id}") val projectId: String,
    @Value("\${google.firebase.service-account}") val serviceAccountString: String,
    private val pushLogger: PushNotificationLogger
) : PushNotificationService {
    init {
        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountString.byteInputStream()))
            .setDatabaseUrl("https://$projectId.firebaseio.com/")
            .build()

        FirebaseApp.initializeApp(options)
    }

    override suspend fun sendMessage(pushMessage: PushTargetMessage, reason: MessageReason) {
        val message = pushMessage.toMessage()
        val response = FirebaseMessaging.getInstance().sendAsync(message).await()
        pushLogger.log(pushMessage.toLog(response, reason))
    }

    // FCM api가 한번에 500개까지 받을 수 있으므로 500개씩 chunk
    override suspend fun sendMessages(pushMessages: List<PushTargetMessage>, reason: MessageReason): Unit = coroutineScope {
        val messagingInstance = FirebaseMessaging.getInstance()
        val responses = pushMessages
            .chunked(500)
            .map { chunk ->
                val messages = chunk.map { it.toMessage() }
                async { messagingInstance.sendAllAsync(messages).await() }
            }
            .awaitAll()
            .flatMap { it.responses }

        pushMessages
            .zip(responses)
            .map { (message, response) -> message.toLog(response.toString(), reason) }
            .let { pushLogger.log(it) }
    }

    override suspend fun sendGlobalMessage(title: String, body: String, reason: MessageReason) {
        sendTopicMessage(TopicMessage("global", title, body), reason)
    }

    override suspend fun sendTopicMessage(pushMessage: TopicMessage, reason: MessageReason) {
        val notification = Notification.builder().setTitle(pushMessage.title).setBody(pushMessage.body).build()
        val message: Message = Message.builder()
            .setNotification(notification)
            .setTopic(pushMessage.topic)
            .build()

        val response = FirebaseMessaging.getInstance().sendAsync(message).await()
        pushLogger.log(pushMessage.toLog(response, reason))
    }

    private fun PushTargetMessage.toLog(response: String, reason: MessageReason): FcmLog = FcmLog(
        to = targetToken,
        message = "$title\n$body",
        author = reason.author,
        cause = reason.cause,
        response = response,
    )

    private fun TopicMessage.toLog(response: String, reason: MessageReason): FcmLog = FcmLog(
        to = topic,
        message = "$title\n$body",
        author = reason.author,
        cause = reason.cause,
        response = response,
    )
}
