package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.lectures.service.LectureService
import com.wafflestudio.snu4t.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class EvHandler(
    private val lectureService: LectureService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiNoAuthMiddleware
) {
    suspend fun getLectureEvaluationSummary(req: ServerRequest): ServerResponse = handle(req) {
        val lectureId = req.pathVariable("lectureId")
        lectureService.getEvSummary(lectureId)
    }
}
