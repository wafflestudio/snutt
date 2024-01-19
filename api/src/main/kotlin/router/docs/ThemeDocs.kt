package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.timetables.dto.TimetableThemeDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableThemeAddRequestDto
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
        path = "/v1/themes", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getThemes",
            parameters = [Parameter(`in` = ParameterIn.QUERY, name = "state", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableThemeDto::class)))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addTheme",
            requestBody = RequestBody(
                content = [Content(schema = Schema(implementation = TimetableThemeAddRequestDto::class))],
                required = true,
            ),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/{themeId}", method = [RequestMethod.PATCH], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "modifyTheme",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "themeId", required = true)],
            requestBody = RequestBody(
                content = [Content(schema = Schema(implementation = TimetableThemeAddRequestDto::class))],
                required = true,
            ),
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/{themeId}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteTheme",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "themeId", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/{themeId}/copy", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "copyTheme",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "themeId", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/{themeId}/default", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "setDefault",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "themeId", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/basic/{basicThemeTypeValue}/default", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "setBasicThemeTypeDefault",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "basicThemeTypeValue", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/{themeId}/default", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "unsetDefault",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "themeId", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
    RouterOperation(
        path = "/v1/themes/basic/{basicThemeTypeValue}/default", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "unsetBasicThemeTypeDefault",
            parameters = [Parameter(`in` = ParameterIn.PATH, name = "basicThemeTypeValue", required = true)],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TimetableThemeDto::class))])],
        ),
    ),
)
annotation class ThemeDocs
