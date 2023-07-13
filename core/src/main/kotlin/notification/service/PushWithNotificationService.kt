package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.notification.data.NotificationType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

interface PushWithNotificationService {
    /**
     * 한 명의 유저에게 푸시를 보내고 알림함에 보이게 합니다.
     */
    suspend fun sendPushAndNotification(pushMessage: PushMessage, notificationType: NotificationType, userId: String)

    /**
     * 복수 명의 유저에게 푸시를 보내고 알림함에 보이게 합니다.
     */
    suspend fun sendPushesAndNotifications(
        pushMessage: PushMessage,
        notificationType: NotificationType,
        userIds: List<String>
    )

    /**
     * 모든 유저에게 푸시를 보내고 해당 내용이 알림함에 보이게 합니다.
     */
    suspend fun sendGlobalPushAndNotification(pushMessage: PushMessage, notificationType: NotificationType)
}

@Service
class PushWithNotificationServiceImpl internal constructor(
    private val pushService: PushService,
    private val notificationService: NotificationService,
) : PushWithNotificationService {
    override suspend fun sendPushAndNotification(
        pushMessage: PushMessage,
        notificationType: NotificationType,
        userId: String,
    ): Unit = coroutineScope {
        launch { notificationService.sendNotification(pushMessage.toNotification(notificationType, userId)) }
        launch { pushService.sendPush(pushMessage, userId) }
    }

    override suspend fun sendPushesAndNotifications(
        pushMessage: PushMessage,
        notificationType: NotificationType,
        userIds: List<String>,
    ): Unit = coroutineScope {
        launch {
            notificationService.sendNotifications(
                userIds.map {
                    pushMessage.toNotification(
                        notificationType, it
                    )
                }
            )
        }
        launch { pushService.sendPushes(pushMessage, userIds) }
    }

    override suspend fun sendGlobalPushAndNotification(
        pushMessage: PushMessage,
        notificationType: NotificationType,
    ): Unit = coroutineScope {
        launch { notificationService.sendNotification(pushMessage.toNotification(notificationType, userId = null)) }
        launch { pushService.sendGlobalPush(pushMessage) }
    }
}
