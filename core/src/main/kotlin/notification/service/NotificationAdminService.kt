package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.exception.InvalidLocalIdException
import com.wafflestudio.snu4t.common.exception.NoUserFcmKeyException
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Service

@Service
class NotificationAdminService(
    private val pushNotificationService: PushNotificationService,
    private val notificationService: NotificationService,
    private val userRepository: UserRepository
) {
    suspend fun insertNotification(body: InsertNotificationRequest): String {
        val receiver = getReceiver(body.userId)

        val notification = body.toNotification(receiver?.id!!)
        notificationService.addNotification(notification)

        if (body.insertFcm) {
            sendPush(receiver, body)
        }

        // 구버전 API 에서 보내주고 있길래 따라함
        return "ok"
    }

    private suspend fun sendPush(receiver: User?, body: InsertNotificationRequest) {
        val messagePayload = PushMessage(body.title, body.body, body.dataPayload)
        if (receiver == null) {
            pushNotificationService.sendGlobalMessage(messagePayload)
        } else {
            val fcmKey = receiver.fcmKey ?: throw NoUserFcmKeyException
            val pushTargetMessage = PushTargetMessage(fcmKey, messagePayload)
            pushNotificationService.sendMessage(pushTargetMessage)
        }
    }

    private suspend fun InsertNotificationRequest.toNotification(receiverId: String?): Notification {
        return Notification(
            userId = receiverId,
            message = body,
            type = type
        )
    }

    private suspend fun getReceiver(receiverId: String?): User? {
        if (receiverId.isNullOrBlank()) {
            return null
        }

        return userRepository.findById(receiverId) ?: throw InvalidLocalIdException
    }
}
