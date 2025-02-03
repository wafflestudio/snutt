package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.handler.SearchQueryLegacy
import com.wafflestudio.snutt.lectures.dto.LectureDto
import io.swagger.v3.oas.annotations.Operation
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
        path = "/v1/search_query",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "searchLecture",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = SearchQueryLegacy::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = LectureDto::class)))],
                    ),
                ],
            ),
    ),
)
annotation class LectureSearchDocs
