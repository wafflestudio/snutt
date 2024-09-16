package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.client.AppVersion
import com.wafflestudio.snu4t.feedback.dto.FeedbackPostRequestDto
import com.wafflestudio.snu4t.feedback.service.FeedbackService
import com.wafflestudio.snu4t.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class FeedbackHandler(
    private val feedbackService: FeedbackService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(snuttRestApiNoAuthMiddleware) {
    suspend fun postFeedback(req: ServerRequest): ServerResponse =
        handle(req) {
            val body = req.awaitBody<FeedbackPostRequestDto>()
            val clientInfo = req.clientInfo!!
            feedbackService.add(
                email = body.email,
                message = body.message,
                osType = clientInfo.osType,
                osVersion = clientInfo.osVersion,
                appVersion = clientInfo.appVersion ?: AppVersion("Unknown"),
                deviceModel = clientInfo.deviceModel ?: "Unknown",
            )
        }
}
