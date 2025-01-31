package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.notification.dto.NotificationCountResponse
import com.wafflestudio.snutt.notification.dto.NotificationQuery
import com.wafflestudio.snutt.notification.dto.NotificationResponse
import com.wafflestudio.snutt.notification.service.NotificationService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class NotificationHandler(
    private val notificationService: NotificationService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiDefaultMiddleware,
    ) {
    suspend fun getNotifications(req: ServerRequest) =
        handle(req) {
            val offset = req.parseQueryParam<Long>("offset") ?: 0
            val limit = req.parseQueryParam<Int>("limit") ?: 20
            val explicit = (req.parseQueryParam<Int>("explicit") ?: 0) > 0

            val notifications = notificationService.getNotifications(NotificationQuery(offset, limit, explicit, req.getContext().user!!))
            notifications.map { NotificationResponse.from(it) }
        }

    suspend fun getUnreadCounts(req: ServerRequest) =
        handle(req) {
            val user = req.getContext().user!!
            val unreadCount = notificationService.getUnreadCount(user)
            NotificationCountResponse(unreadCount)
        }
}
