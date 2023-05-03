package com.wafflestudio.snu4t.common.push.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.common.push.dto.TopicMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class FcmPushNotificationService(
    @Value("\${google.firebase.project-id}") val projectId: String,
    @Value("\${google.firebase.service-account}") val serviceAccountString: String,
) : PushNotificationService {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountString.byteInputStream()))
            .setDatabaseUrl("https://$projectId.firebaseio.com/")
            .build()

        FirebaseApp.initializeApp(options)
    }

    override suspend fun sendMessage(pushMessage: PushTargetMessage) {
        val message = pushMessage.toFcmMessage()
        val response = FirebaseMessaging.getInstance().sendAsync(message).await()
        logger.info("Message Request Sent : $message, response : $response")
    }

    // FCM api가 한번에 500개까지 받을 수 있으므로 500개씩 chunk
    override suspend fun sendMessages(pushMessages: List<PushTargetMessage>): Unit = coroutineScope {
        val messagingInstance = FirebaseMessaging.getInstance()
        val responses = pushMessages
            .chunked(500)
            .map { chunk ->
                val messages = chunk.map { it.toFcmMessage() }
                async { messagingInstance.sendAllAsync(messages).await() }
            }
            .awaitAll()
            .flatMap { it.responses }

        pushMessages
            .zip(responses)
            .map { (message, response) -> logger.info("Message Request Sent: $message, response : $response") }
    }

    override suspend fun sendGlobalMessage(payload: PushMessage) {
        sendTopicMessage(TopicMessage("global", payload))
    }

    override suspend fun sendTopicMessage(pushMessage: TopicMessage) {
        val message = pushMessage.toFcmMessage()
        val response = FirebaseMessaging.getInstance().sendAsync(message).await()
        logger.info("Message Request Sent : $message, response : $response")
    }
}
