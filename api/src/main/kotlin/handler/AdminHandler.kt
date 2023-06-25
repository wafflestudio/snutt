package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.OkResponse
import com.wafflestudio.snu4t.middleware.SnuttRestAdminApiMiddleware
import com.wafflestudio.snu4t.notification.service.NotificationAdminService
import notification.dto.InsertNotificationRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class AdminHandler(
    private val notificationAdminService: NotificationAdminService,
    snuttRestAdminApiMiddleware: SnuttRestAdminApiMiddleware,
) : ServiceHandler(snuttRestAdminApiMiddleware) {
    suspend fun insertNotification(req: ServerRequest) = handle(req) {
        val body = req.awaitBody<InsertNotificationRequest>()
        notificationAdminService.insertNotification(body)

        OkResponse()
    }
}
