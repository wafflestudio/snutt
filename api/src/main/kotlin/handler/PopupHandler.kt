package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import com.wafflestudio.snutt.popup.dto.PopupResponse
import com.wafflestudio.snutt.popup.service.PopupService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class PopupHandler(
    private val popupService: PopupService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(snuttRestApiNoAuthMiddleware) {
    suspend fun getPopups(req: ServerRequest) =
        handle(req) {
            val clientInfo = req.clientInfo!!

            val popups = popupService.getPopups(clientInfo)

            ListResponse(
                content = popups.map(::PopupResponse),
                totalCount = popups.size,
            )
        }
}
