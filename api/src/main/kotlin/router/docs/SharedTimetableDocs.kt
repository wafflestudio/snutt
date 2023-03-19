package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.bookmark.dto.BookmarkLectureModifyRequest
import com.wafflestudio.snu4t.bookmark.dto.BookmarkResponse
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableBriefDto
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableCreateRequest
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableModifyRequest
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
            parameters = [
                Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = SharedTimetableBriefDto::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getSharedTimetables",
            parameters = [
                Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true),
                Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = SharedTimetableBriefDto::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addSharedTimetable",
            parameters = [Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true)],
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = SharedTimetableCreateRequest::class))]),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "updateSharedTimetables",
            parameters = [
                Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true),
                Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = SharedTimetableModifyRequest::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/shared-tables/", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteSharedTimetable",
            parameters = [Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true),
                         Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = BookmarkLectureModifyRequest::class))]),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
)
annotation class SharedTimetableDocs