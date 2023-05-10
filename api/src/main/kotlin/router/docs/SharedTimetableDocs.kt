package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableBriefDto
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableCreateRequest
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableDetailDto
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableModifyRequest
import com.wafflestudio.snu4t.timetables.data.Timetable
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
        path = "/v1/shared-tables", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getSharedTimetables",
            parameters = [],
            responses = [ApiResponse(responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = SharedTimetableBriefDto::class)))])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getSharedTimetables",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = SharedTimetableDetailDto::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addSharedTimetable",
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = SharedTimetableCreateRequest::class))]),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/{id}", method = [RequestMethod.PUT], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "updateSharedTimetables",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)
            ],
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = SharedTimetableModifyRequest::class))]),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/{id}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteSharedTimetable",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/{id}/copy", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "copySharedTimetable",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "sharedTimetableId", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = Timetable::class))])]
        ),
    ),
)
annotation class SharedTimetableDocs
