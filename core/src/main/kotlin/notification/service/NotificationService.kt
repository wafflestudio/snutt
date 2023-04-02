package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.dto.NotificationQuery
import com.wafflestudio.snu4t.notification.repository.NotificationRepository
import com.wafflestudio.snu4t.notification.repository.countUnreadNotifications
import com.wafflestudio.snu4t.notification.repository.findNotifications
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.service.UserService
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService(
    private val repository: NotificationRepository,
    private val userService: UserService
) {
    suspend fun getNotification(query: NotificationQuery): List<Notification> {
        val user = query.user
        val notifications = repository.findNotifications(
            userId = user.id!!,
            createdAt = user.regDate,
            pageable = PageRequest.of(query.offset, query.limit)
        ).toList()

        if (query.explicit) {
            userService.update(user.apply { notificationCheckedAt = LocalDateTime.now() })
        }

        return notifications
    }

    suspend fun getUnreadCount(user: User): Long {
        return repository.countUnreadNotifications(user.id!!, user.notificationCheckedAt)
    }

    // TODO LINK_ADDR 타입 추가될 경우 detail 체크 필요
    suspend fun addNotification(notification: Notification): Notification {
        //  신규 추가되는 것인지 체크
        check(notification.id == null)
        return repository.save(notification)
    }
}
