package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.exception.UserNotFoundException
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.users.repository.UserRepository
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Service

@Service
class NotificationAdminService(
    private val pushWithNotificationService: PushWithNotificationService,
    private val notificationService: NotificationService,
    private val userRepository: UserRepository,
) {
    suspend fun insertNotification(request: InsertNotificationRequest) {
        val user = request.userId?.let {
            userRepository.findByCredentialLocalIdAndActiveTrue(it) ?: throw UserNotFoundException
        }

        val pushMessage = PushMessage(
            request.title,
            request.body,
            request.dataPayload,
        )
        val notificationType = request.type

        if (request.insertFcm) {
            user?.let {
                pushWithNotificationService.sendPushAndNotification(pushMessage, notificationType, it.id!!)
            } ?: run {
                pushWithNotificationService.sendGlobalPushAndNotification(pushMessage, notificationType)
            }
        } else {
            notificationService.sendNotification(pushMessage.toNotification(notificationType, user?.id))
        }
    }
}
