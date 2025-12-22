package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.client.AppVersion
import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.feedback.dto.FeedbackPostRequestDto
import com.wafflestudio.snutt.feedback.service.FeedbackService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/feedback", "/feedback")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @PostMapping("")
    suspend fun postFeedback(
        @RequestBody body: FeedbackPostRequestDto,
        @RequestAttribute("clientInfo") clientInfo: ClientInfo,
    ): OkResponse {
        feedbackService.addGithubIssue(
            email = body.email,
            message = body.message,
            osType = clientInfo.osType,
            osVersion = clientInfo.osVersion,
            appVersion = clientInfo.appVersion ?: AppVersion("Unknown"),
            deviceModel = clientInfo.deviceModel ?: "Unknown",
        )
        return OkResponse()
    }
}
