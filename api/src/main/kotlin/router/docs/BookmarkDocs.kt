package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.bookmark.dto.BookmarkLectureModifyRequest
import com.wafflestudio.snu4t.bookmark.dto.BookmarkResponse
import com.wafflestudio.snu4t.common.dto.ExistenceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
        path = "/v1/bookmarks", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getBookmark",
            parameters = [
                Parameter(`in` = ParameterIn.QUERY, name = "year", required = true),
                Parameter(`in` = ParameterIn.QUERY, name = "semester", required = true),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = BookmarkResponse::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/bookmarks/lectures/{lectureId}/state", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "existsBookmarkLecture",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "lectureId", required = true)],
            responses = [
                ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ExistenceResponse::class))]),
            ]
        ),
    ),
    RouterOperation(
        path = "/v1/bookmarks/lecture", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addLecture",
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = BookmarkLectureModifyRequest::class), mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
    RouterOperation(
        path = "/v1/bookmarks/lecture", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteBookmark",
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = BookmarkLectureModifyRequest::class), mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
)
annotation class BookmarkDocs
