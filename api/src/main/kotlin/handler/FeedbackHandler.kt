package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.feedback.dto.FeedbackPostRequestDto
import com.wafflestudio.snu4t.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class FeedbackHandler(
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(snuttRestApiNoAuthMiddleware) {
    suspend fun postFeedback(req: ServerRequest): ServerResponse =
        handle(req) {
            val body = req.awaitBody<FeedbackPostRequestDto>()
            val clientInfo = req.clientInfo!!
        }
}
