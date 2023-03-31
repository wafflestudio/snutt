package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.notification.data.Notification
import com.wafflestudio.snu4t.notification.dto.NotificationQuery
import com.wafflestudio.snu4t.notification.repository.NotificationRepository
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
        val notifications = repository.findAllByUserIdAndCreatedAtGreaterThan(
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
        return repository.countByUserIdAndCreatedAtAfter(user.id!!, user.notificationCheckedAt)
    }
}
