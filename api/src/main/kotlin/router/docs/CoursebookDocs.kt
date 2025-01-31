package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.coursebook.data.CoursebookOfficialResponse
import com.wafflestudio.snutt.coursebook.data.CoursebookResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/course_books",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getAllCoursebooks",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = CoursebookResponse::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/course_books/recent",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getMostRecentCoursebook",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = CoursebookResponse::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/course_books/official",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getSyllabusUrl",
                parameters = [
                    Parameter(`in` = ParameterIn.QUERY, name = "year", required = true, example = "2024"),
                    Parameter(`in` = ParameterIn.QUERY, name = "semester", required = true, example = "3"),
                    Parameter(`in` = ParameterIn.QUERY, name = "course_number", required = true, example = "M1522.001400"),
                    Parameter(`in` = ParameterIn.QUERY, name = "lecture_number", required = true, example = "001"),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = CoursebookOfficialResponse::class))],
                    ),
                ],
            ),
    ),
)
annotation class CoursebookDocs()
