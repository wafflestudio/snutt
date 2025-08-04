package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.notification.data.Notification
import com.wafflestudio.snutt.notification.dto.NotificationQuery
import com.wafflestudio.snutt.notification.repository.NotificationRepository
import com.wafflestudio.snutt.notification.repository.countUnreadNotifications
import com.wafflestudio.snutt.users.data.User
import com.wafflestudio.snutt.users.service.UserService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 알림함의 알림을 관리하는 서비스입니다.
 */
interface NotificationService {
    suspend fun getNotifications(query: NotificationQuery): List<Notification>

    suspend fun getUnreadCount(user: User): Long

    /**
     * 푸시를 보내지 않고 알림함에 보일 [Notification] 만 저장하므로 사용 시 주의가 필요합니다.
     *
     * @see [PushWithNotificationService.sendPushAndNotification]
     */
    suspend fun sendNotification(notification: Notification)

    /**
     * 푸시를 보내지 않고 알림함에 보일 [Notification] 만 저장하므로 사용 시 주의가 필요합니다.
     *
     * @see [PushWithNotificationService.sendPushesAndNotifications]
     */
    suspend fun sendNotifications(notifications: List<Notification>)
}

@Service
class NotificationServiceImpl(
    private val userService: UserService,
    private val notificationRepository: NotificationRepository,
) : NotificationService {
    override suspend fun getNotifications(query: NotificationQuery): List<Notification> {
        val user = query.user
        val notifications =
            notificationRepository
                .findNotifications(
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

    override suspend fun getUnreadCount(user: User): Long =
        notificationRepository.countUnreadNotifications(user.id!!, user.notificationCheckedAt)

    override suspend fun sendNotification(notification: Notification) {
        notificationRepository.save(notification)
    }

    override suspend fun sendNotifications(notifications: List<Notification>) {
        notificationRepository.saveAll(notifications).collect()
    }
}
