package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/ev/lectures/{lectureId}/summary",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getLectureEvaluationSummary",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "lectureId", required = true, description = "snutt lecture id")],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = SnuttEvLectureSummaryDto::class))],
                    ),
                ],
            ),
    ),
)
annotation class EvDocs
