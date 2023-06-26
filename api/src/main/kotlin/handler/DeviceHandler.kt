package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.OkResponse
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.notification.service.DeviceService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class DeviceHandler(
    private val deviceService: DeviceService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun addRegistrationId(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val clientInfo = req.clientInfo!!

        val registrationId = req.pathVariable("id")
        deviceService.addRegistrationId(userId, registrationId, clientInfo)

        OkResponse()
    }

    suspend fun removeRegistrationId(req: ServerRequest) = handle(req) {
        val userId = req.userId

        val registrationId = req.pathVariable("id")
        deviceService.removeRegistrationId(userId, registrationId)

        OkResponse()
    }
}
