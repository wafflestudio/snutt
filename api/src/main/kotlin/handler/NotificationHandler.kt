package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.notification.dto.NotificationQuery
import com.wafflestudio.snu4t.notification.service.NotificationService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class NotificationHandler(
    private val notificationService: NotificationService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiDefaultMiddleware
) {
    suspend fun getNotification(req: ServerRequest) = handle(req) {
        val offset = req.parseQueryParam<Int>("offset") ?: 0
        val limit = req.parseQueryParam<Int>("limit") ?: 20
        val explicit = (req.parseQueryParam<Int>("explicit") ?: 0) > 0
        notificationService.getNotification(NotificationQuery(offset, limit, explicit, req.getContext().user!!))
    }

    suspend fun getUnreadCounts(req: ServerRequest) = handle(req) {
        val user = req.getContext().user!!
        notificationService.getUnreadCount(user)
    }
}
