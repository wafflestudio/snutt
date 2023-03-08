package com.wafflestudio.snu4t.common.push

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.wafflestudio.snu4t.common.secret.SecretRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

interface PushNotificationService {
    suspend fun sendMessage(pushMessage: PushTargetMessage)
    suspend fun sendMessages(pushMessages: List<PushTargetMessage>)
    suspend fun sendGlobalMessage(title: String, body: String)
    suspend fun sendTopicMessage(pushMessage: TopicMessage)
}

@Service
class FcmPushNotificationService(
    @Value("\${fcm.project-id}") val projectId: String,
    @Value("\${fcm.secret-names}") val fcmSecretNames: String,
    secretRepository: SecretRepository
) : PushNotificationService {
    init {
        val googleAccountSecret = secretRepository.getSecretString(fcmSecretNames).byteInputStream()

        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(googleAccountSecret))
            .setDatabaseUrl("https://$projectId.firebaseio.com/")
            .build()

        FirebaseApp.initializeApp(options)
    }

    override suspend fun sendMessage(pushMessage: PushTargetMessage) {
        val notification = Notification.builder().setTitle(pushMessage.title).setBody(pushMessage.body).build()
        val message: Message = Message.builder()
            .setNotification(notification)
            .setToken(pushMessage.targetToken)
            .build()
        FirebaseMessaging.getInstance().sendAsync(message).await()
    }

    // FCM api가 한번에 500개까지 받을 수 있으므로 500개씩 chunk
    override suspend fun sendMessages(pushMessages: List<PushTargetMessage>): Unit = coroutineScope {
        val messagingInstance = FirebaseMessaging.getInstance()
        pushMessages.map { pushMessage ->
            val notification = Notification.builder().setTitle(pushMessage.title).setBody(pushMessage.body).build()
            Message.builder()
                .setNotification(notification)
                .setToken(pushMessage.targetToken)
                .build()
        }.chunked(500).map {
            async { messagingInstance.sendAllAsync(it).await() }
        }.awaitAll()
    }

    override suspend fun sendGlobalMessage(title: String, body: String) {
        sendTopicMessage(TopicMessage("global", title, body))
    }

    override suspend fun sendTopicMessage(pushMessage: TopicMessage) {
        val notification = Notification.builder().setTitle(pushMessage.title).setBody(pushMessage.body).build()
        val message: Message = Message.builder()
            .setNotification(notification)
            .setTopic(pushMessage.topic)
            .build()
        FirebaseMessaging.getInstance().sendAsync(message).await()
    }
}

data class PushTargetMessage(
    val targetToken: String,
    val title: String,
    val body: String,
)

data class TopicMessage(
    val topic: String,
    val title: String,
    val body: String,
)
