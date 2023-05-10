package com.wafflestudio.snu4t.router.docs

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
import timetables.dto.TimetableBriefDto

@RouterOperations(
    RouterOperation(
        path = "/v1/search_query", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "hello",
            parameters = [Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableBriefDto::class))])]
        ),
    )
)
annotation class LectureDocs
