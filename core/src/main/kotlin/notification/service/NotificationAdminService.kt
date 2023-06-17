package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.exception.NoUserFcmKeyException
import com.wafflestudio.snu4t.common.exception.UserNotFoundException
import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.coroutineScope
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Service

@Service
class NotificationAdminService(
    private val pushNotificationService: PushNotificationService,
    private val deviceService: DeviceService,
    private val notificationService: NotificationService,
    private val userRepository: UserRepository
) {
    suspend fun insertNotification(request: InsertNotificationRequest) = coroutineScope {
        val user = request.userId?.let {
            userRepository.findByCredentialLocalIdAndActiveTrue(it) ?: throw UserNotFoundException
        }

        notificationService.addNotification(
            Notification(
                userId = user?.id,
                message = request.body,
                type = request.type
            )
        )

        user?.let {
            sendPush(it, request)
        } ?: run {
            sendGlobalPush(request)
        }
    }

    private suspend fun sendPush(user: User, request: InsertNotificationRequest) {
        if (!request.insertFcm) return

        val message = PushMessage(request.title, request.body, request.dataPayload)
        val userDevices = deviceService.getUserDevices(user.id!!).ifEmpty { throw NoUserFcmKeyException }

        val pushMessages = userDevices.map { PushTargetMessage(it.fcmRegistrationId, message) }
        pushNotificationService.sendMessages(pushMessages)
    }

    private suspend fun sendGlobalPush(request: InsertNotificationRequest) {
        if (!request.insertFcm) return

        val message = PushMessage(request.title, request.body, request.dataPayload)
        pushNotificationService.sendGlobalMessage(message)
    }
}
