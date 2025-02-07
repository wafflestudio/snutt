package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.feedback.dto.FeedbackPostRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/feedback",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "postFeedback",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = FeedbackPostRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = OkResponse::class))],
                    ),
                ],
            ),
    ),
)
annotation class FeedbackDocs()
