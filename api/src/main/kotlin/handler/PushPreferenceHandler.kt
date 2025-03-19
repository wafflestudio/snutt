package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.notification.dto.PushPreferenceDto
import com.wafflestudio.snutt.notification.service.PushPreferenceService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class PushPreferenceHandler(
    private val pushPreferenceService: PushPreferenceService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiDefaultMiddleware,
    ) {
    suspend fun getPushPreferences(req: ServerRequest) =
        handle(req) {
            val user = req.getContext().user!!
            pushPreferenceService.getPushPreferenceDto(user)
        }

    suspend fun savePushPreferences(req: ServerRequest) =
        handle(req) {
            val user = req.getContext().user!!
            val pushPreferenceDto = req.awaitBody<PushPreferenceDto>()
            pushPreferenceService.savePushPreference(user, pushPreferenceDto)
        }
}
