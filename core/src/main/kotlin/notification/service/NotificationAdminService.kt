package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.exception.InvalidLocalIdException
import com.wafflestudio.snu4t.common.exception.NoUserFcmKeyException
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import common.push.dto.MessagePayload
import common.push.dto.MessageReason
import common.push.dto.PushTargetMessage
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Service

@Service
class NotificationAdminService(
    private val pushNotificationService: PushNotificationService,
    private val notificationService: NotificationService,
    private val userRepository: UserRepository
) {
    suspend fun insertNotification(senderId: String, body: InsertNotificationRequest): String {
        val receiver = getReceiver(body.userId)

        val notification = body.toNotification(receiver?.id!!)
        notificationService.addNotification(notification)

        if (body.insertFcm) {
            sendPush(senderId, receiver, body)
        }

        // 구버전 API 에서 보내주고 있길래 따라함
        return "ok"
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
