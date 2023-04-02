package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dynamiclink.client.DynamicLinkClient
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.service.TimetableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class TimetableHandler(
    private val timeTableService: TimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiDefaultMiddleware
) {
    suspend fun getBriefs(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId

        timeTableService.getBriefs(userId = userId)
    }

    suspend fun getLink(req: ServerRequest): ServerResponse = handle(req) {
        val timetableId = req.pathVariable("id")
        timeTableService.getLink(timetableId)
    }
}
