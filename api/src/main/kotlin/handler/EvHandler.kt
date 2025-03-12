package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class EvHandler(
    private val evService: EvService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiNoAuthMiddleware,
    ) {
    suspend fun getLectureEvaluationSummary(req: ServerRequest): ServerResponse =
        handle(req) {
            val lectureId = req.pathVariable("lectureId")
            evService.getEvSummary(lectureId)
        }
}
