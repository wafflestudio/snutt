package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.dto.NotificationQuery
import com.wafflestudio.snu4t.users.data.User

/**
 * 알림함의 알림을 관리하는 서비스입니다.
 */
interface NotificationService {
    suspend fun getNotifications(query: NotificationQuery): List<Notification>

    suspend fun getUnreadCount(user: User): Long

    /**
     * 푸시를 보내지 않고 알림함에 보일 [Notification] 만 저장하므로 사용 시 주의가 필요합니다.
     *
     * @see [PushNotificationService.sendPushAndNotification]
     */
    suspend fun sendNotification(notification: Notification)

    /**
     * 푸시를 보내지 않고 알림함에 보일 [Notification] 만 저장하므로 사용 시 주의가 필요합니다.
     *
     * @see [PushNotificationService.sendPushesAndNotifications]
     */
    suspend fun sendNotifications(notifications: List<Notification>)
}
