package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.notification.dto.NotificationCountResponse
import com.wafflestudio.snutt.notification.dto.NotificationQuery
import com.wafflestudio.snutt.notification.dto.NotificationResponse
import com.wafflestudio.snutt.notification.service.NotificationService
import com.wafflestudio.snutt.users.data.User
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/notification",
    "/notification",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class NotificationController(
    private val notificationService: NotificationService,
) {
    @GetMapping("")
    suspend fun getNotifications(
        @CurrentUser user: User,
        @RequestParam(required = false, defaultValue = "0") offset: Long,
        @RequestParam(required = false, defaultValue = "20") limit: Int,
        @RequestParam(required = false, defaultValue = "0") explicit: Int,
    ): List<NotificationResponse> {
        val notifications =
            notificationService.getNotifications(
                NotificationQuery(offset, limit, explicit > 0, user),
            )
        return notifications.map { NotificationResponse.from(it) }
    }

    @GetMapping("/count")
    suspend fun getUnreadCounts(
        @CurrentUser user: User,
    ): NotificationCountResponse {
        val unreadCount = notificationService.getUnreadCount(user)
        return NotificationCountResponse(unreadCount)
    }
}
