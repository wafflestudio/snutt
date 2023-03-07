package com.wafflestudio.snu4t.notification.repository

import com.wafflestudio.snu4t.notification.data.Notification
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : CoroutineCrudRepository<Notification, String> {
}
