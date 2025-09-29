package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.diary.dto.DiaryActivityDto
import com.wafflestudio.snutt.diary.dto.DiaryQuestionnaireDto
import com.wafflestudio.snutt.diary.dto.DiarySubmissionsOfYearSemesterDto
import com.wafflestudio.snutt.diary.dto.request.DiaryQuestionnaireRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
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
        path = "/v1/diary/my",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getMySubmissions",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [
                            Content(
                                array = ArraySchema(schema = Schema(implementation = DiarySubmissionsOfYearSemesterDto::class)),
                            ),
                        ],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/diary/questionnaire",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getQuestionnaireFromActivities",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = DiaryQuestionnaireRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                        required = true,
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = DiaryQuestionnaireDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/diary/activities",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getActivities",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = DiaryActivityDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/diary",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "submitDiary",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = DiarySubmissionRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                        required = true,
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = OkResponse::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/diary/{id}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "removeDiarySubmission",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "id", required = true)],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = OkResponse::class))],
                    ),
                ],
            ),
    ),
)
annotation class DiaryDocs
