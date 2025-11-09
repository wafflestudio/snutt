package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.common.exception.UserNotFoundException
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.users.repository.UserRepository
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Service

@Service
class NotificationAdminService(
    private val pushWithNotificationService: PushWithNotificationService,
    private val notificationService: NotificationService,
    private val userRepository: UserRepository,
) {
    suspend fun insertNotification(request: InsertNotificationRequest) {
        val user =
            request.userId?.let {
                userRepository.findByIdAndActiveTrue(it) ?: throw UserNotFoundException
            }

        val pushMessage =
            PushMessage(
                title = request.title,
                body = request.body,
                data = PushMessage.Data(request.dataPayload),
                shouldSendAsDataMessage = request.shouldSendAsDataMessage,
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
