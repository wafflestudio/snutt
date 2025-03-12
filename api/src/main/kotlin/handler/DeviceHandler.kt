package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.exception.InvalidPathParameterException
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.notification.service.DeviceService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class DeviceHandler(
    private val deviceService: DeviceService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun addRegistrationId(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val clientInfo = req.clientInfo!!

            val registrationId = req.pathVariable("id")
            if (registrationId.isBlank()) throw InvalidPathParameterException("id")
            deviceService.addRegistrationId(userId, registrationId, clientInfo)

            OkResponse()
        }

    suspend fun removeRegistrationId(req: ServerRequest) =
        handle(req) {
            val userId = req.userId

            val registrationId = req.pathVariable("id")
            if (registrationId.isBlank()) throw InvalidPathParameterException("id")
            deviceService.removeRegistrationId(userId, registrationId)

            OkResponse()
        }
}
