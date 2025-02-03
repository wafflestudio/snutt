package com.wafflestudio.snutt.common.push.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.wafflestudio.snutt.common.push.PushClient
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithTopic
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!test")
internal class FcmPushClient(
    @Value("\${google.firebase.project-id}") private val projectId: String,
    @Value("\${google.firebase.service-account}") private val serviceAccountString: String,
) : PushClient {
    private object PayloadKeys {
        const val URL_SCHEME = "url_scheme"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        val options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountString.byteInputStream()))
                .setDatabaseUrl("https://$projectId.firebaseio.com/")
                .build()

        FirebaseApp.initializeApp(options)
    }

    companion object {
        private const val FCM_MESSAGE_COUNT_LIMIT = 500
        private const val GLOBAL_TOPIC = "global"
    }

    override suspend fun sendMessage(pushMessage: TargetedPushMessageWithToken) {
        val message = pushMessage.toFcmMessage()
        val response = FirebaseMessaging.getInstance().sendAsync(message).await()
        log.info("Message Request Sent : $message, response : $response")
    }

    override suspend fun sendMessages(pushMessages: List<TargetedPushMessageWithToken>): Unit =
        coroutineScope {
            val messagingInstance = FirebaseMessaging.getInstance()
            val responses =
                pushMessages
                    .chunked(FCM_MESSAGE_COUNT_LIMIT)
                    .map { chunk ->
                        val messages = chunk.map { it.toFcmMessage() }
                        async {
                            runCatching {
                                messagingInstance.sendEachAsync(messages).await()
                            }.getOrElse {
                                log.error("푸시전송 실패", it)
                                null
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
                    .flatMap { it.responses }

            pushMessages
                .zip(responses)
                .map { (message, response) -> log.info("Message Request Sent: $message, response : $response") }
        }

    override suspend fun sendGlobalMessage(pushMessage: PushMessage) {
        sendTopicMessage(TargetedPushMessageWithTopic(GLOBAL_TOPIC, pushMessage))
    }

    override suspend fun sendTopicMessage(pushMessage: TargetedPushMessageWithTopic) {
        val message = pushMessage.toFcmMessage()
        val response = FirebaseMessaging.getInstance().sendAsync(message).await()
        log.info("Message Request Sent : $message, response : $response")
    }

    override suspend fun subscribeGlobalTopic(registrationId: String) {
        FirebaseMessaging.getInstance().subscribeToTopicAsync(listOf(registrationId), GLOBAL_TOPIC).await()
        log.debug("Subscribed to global topic: $registrationId")
    }

    override suspend fun unsubscribeGlobalTopic(registrationId: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopicAsync(listOf(registrationId), GLOBAL_TOPIC).await()
        log.debug("Unsubscribed from global topic: $registrationId")
    }

    private fun TargetedPushMessage.toFcmMessage(): Message {
        val notification =
            Notification.builder()
                .setTitle(message.title)
                .setBody(message.body)
                .build()
        return Message.builder().run {
            when (this@toFcmMessage) {
                is TargetedPushMessageWithToken -> setToken(targetToken)
                is TargetedPushMessageWithTopic -> setTopic(topic)
            }
            message.urlScheme?.let { putData(PayloadKeys.URL_SCHEME, it.build().value) }
            setNotification(notification)
            putAllData(message.data.payload)
            build()
        }
    }
}
