package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.client.AppVersion
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.feedback.dto.FeedbackPostRequestDto
import com.wafflestudio.snutt.feedback.service.FeedbackService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
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
            feedbackService.addGithubIssue(
                email = body.email,
                message = body.message,
                osType = clientInfo.osType,
                osVersion = clientInfo.osVersion,
                appVersion = clientInfo.appVersion ?: AppVersion("Unknown"),
                deviceModel = clientInfo.deviceModel ?: "Unknown",
            )
            OkResponse()
        }
}
