package com.wafflestudio.snu4t.common.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.wafflestudio.snu4t.common.secret.SecretRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

interface PushNotificationService {
    fun sendMessage(token: String, title:String, body: String)
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

    override fun sendMessage(token: String, title:String, body: String) {
        val notification = Notification.builder().setTitle(title).setBody(body).build()
        val message: Message = Message.builder()
            .setNotification(notification)
            .setToken(token)
            .build()
        FirebaseMessaging.getInstance().send(message)
    }
}
