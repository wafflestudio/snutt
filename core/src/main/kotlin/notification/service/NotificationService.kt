package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.push.PushClient
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.dto.NotificationQuery
import com.wafflestudio.snu4t.notification.repository.NotificationRepository
import com.wafflestudio.snu4t.notification.repository.countUnreadNotifications
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.service.UserService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService internal constructor(
    private val userService: UserService,
    private val deviceService: DeviceService,
    private val pushClient: PushClient,
    private val notificationRepository: NotificationRepository,
) {
    suspend fun getNotifications(query: NotificationQuery): List<Notification> {
        val user = query.user
        val notifications = notificationRepository.findNotifications(
            userId = user.id!!,
            createdAt = user.regDate,
            offset = query.offset,
            limit = query.limit,
        ).toList()

        if (query.explicit) {
            userService.update(user.apply { notificationCheckedAt = LocalDateTime.now() })
        }

        return notifications
    }

    suspend fun getUnreadCount(user: User): Long {
        return notificationRepository.countUnreadNotifications(user.id!!, user.notificationCheckedAt)
    }

    suspend fun sendPush(userId: String, pushMessage: PushMessage) = coroutineScope {
        launch { saveNotification(userId, pushMessage) }

        if (!pushMessage.notify) return@coroutineScope
        launch {
            val userDevices = deviceService.getUserDevices(userId).ifEmpty { return@launch }

            val pushTargetMessages = userDevices.map { PushTargetMessage(it.fcmRegistrationId, pushMessage) }
            pushClient.sendMessages(pushTargetMessages)
        }
    }

    suspend fun sendPushes(userIds: List<String>, pushMessage: PushMessage) = coroutineScope {
        launch { saveNotifications(userIds, pushMessage) }

        if (!pushMessage.notify) return@coroutineScope
        launch {
            val userIdToDevices = deviceService.getUsersDevices(userIds).ifEmpty { return@launch }

            val pushTargetMessages = userIdToDevices.values.flatMap { userDevices ->
                userDevices.map { PushTargetMessage(it.fcmRegistrationId, pushMessage) }
            }

            pushClient.sendMessages(pushTargetMessages)
        }
    }

    suspend fun sendGlobalPush(pushMessage: PushMessage) = coroutineScope {
        launch { saveNotification(userId = null, pushMessage) }

        if (!pushMessage.notify) return@coroutineScope
        launch { pushClient.sendGlobalMessage(pushMessage) }
    }

    private suspend fun saveNotification(userId: String?, pushMessage: PushMessage) {
        val notification = pushMessage.toNotification(userId)
        notificationRepository.save(notification)
    }

    private suspend fun saveNotifications(userIds: List<String>, pushMessage: PushMessage) {
        val notifications = userIds.map { pushMessage.toNotification(it) }
        notificationRepository.saveAll(notifications).collect()
    }
}
