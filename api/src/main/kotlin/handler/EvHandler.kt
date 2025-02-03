package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class EvHandler(
    private val lectureService: LectureService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiNoAuthMiddleware,
    ) {
    suspend fun getLectureEvaluationSummary(req: ServerRequest): ServerResponse =
        handle(req) {
            val lectureId = req.pathVariable("lectureId")
            lectureService.getEvSummary(lectureId)
        }
}
