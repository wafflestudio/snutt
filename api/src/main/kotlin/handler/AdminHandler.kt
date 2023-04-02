package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.exception.InvalidLocalIdException
import com.wafflestudio.snu4t.common.exception.NoUserFcmKeyException
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.middleware.SnuttRestAdminApiMiddleware
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.service.NotificationService
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import common.push.dto.MessagePayload
import common.push.dto.MessageReason
import common.push.dto.PushTargetMessage
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class AdminHandler(
    private val notificationService: NotificationService,
    private val pushNotificationService: PushNotificationService,
    private val userRepository: UserRepository,
    snuttRestAdminApiMiddleware: SnuttRestAdminApiMiddleware,
) : ServiceHandler(snuttRestAdminApiMiddleware) {

    suspend fun insertNotification(req: ServerRequest) = handle(req) {
        val body = req.awaitBody<InsertNotificationRequest>()
        val receiver = getReceiver(body.userId)

        val notification = body.toNotification(receiver?.id!!)
        notificationService.addNotification(notification)

        if (body.insertFcm) {
            sendPush(req.userId, receiver, body)
        }

        return@handle "ok"
    }

    private suspend fun sendPush(senderId: String, receiver: User?, body: InsertNotificationRequest) {
        val messagePayload = MessagePayload(body.title, body.body, body.urlScheme)
        val messageReason = MessageReason(senderId, "admin")
        if (receiver == null) {
            pushNotificationService.sendGlobalMessage(messagePayload, messageReason)
        } else {
            val fcmKey = receiver.fcmKey ?: throw NoUserFcmKeyException
            val pushTargetMessage = PushTargetMessage(fcmKey, messagePayload)
            pushNotificationService.sendMessage(pushTargetMessage, messageReason)
        }
    }

    private suspend fun InsertNotificationRequest.toNotification(receiverId: String?): Notification {
        return Notification(
            userId = receiverId,
            message = body,
            type = type,
            link = urlScheme,
        )
    }

    private suspend fun getReceiver(receiverId: String?): User? {
        if (receiverId.isNullOrBlank()) {
            return null
        }

        return userRepository.findById(receiverId) ?: throw InvalidLocalIdException
    }
}
