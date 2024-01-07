package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.timetables.dto.TimetableLegacyDto
import com.wafflestudio.snu4t.timetables.dto.request.CustomTimetableLectureAddLegacyRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableLectureModifyLegacyRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableModifyRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableModifyThemeRequestDto
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
import timetables.dto.TimetableBriefDto

@RouterOperations(
    RouterOperation(
        path = "/v1/tables", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getBrief",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/recent",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getMostRecentlyUpdatedTimetables",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{year}/{semester}",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getTimetablesBySemester",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableLegacyDto::class)))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addTimetable",
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = TimetableAddRequestDto::class))]),
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getTimetable",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "modifyTimetable",
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = TimetableModifyRequestDto::class))]),
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteTimetable",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/copy",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "copyTimetable",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/theme",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "modifyTimetableTheme",
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = TimetableModifyThemeRequestDto::class))]),
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/primary",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "setPrimary",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = Unit::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/primary",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "unSetPrimary",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = Unit::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addCustomLecture",
            parameters = [
                Parameter(
                    `in` = ParameterIn.QUERY,
                    name = "isForced",
                    required = false,
                    description = "시간 겹치는 강의 강제로 삭제 후 실행"
                ),
            ],
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = CustomTimetableLectureAddLegacyRequestDto::class))]),
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{lectureId}",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addLecture",
            parameters = [
                Parameter(
                    `in` = ParameterIn.QUERY,
                    name = "isForced",
                    required = false,
                    description = "시간 겹치는 강의 강제로 삭제 후 실행"
                ),
            ],
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}/reset",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "resetTimetableLecture",
            parameters = [
                Parameter(
                    `in` = ParameterIn.QUERY,
                    name = "isForced",
                    required = false,
                    description = "시간 겹치는 강의 강제로 삭제 후 실행"
                ),
            ],
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "modifyTimetableLecture",
            parameters = [
                Parameter(
                    `in` = ParameterIn.QUERY,
                    name = "isForced",
                    required = false,
                    description = "시간 겹치는 강의 강제로 삭제 후 실행"
                ),
            ],
            requestBody = RequestBody(content = [Content(schema = Schema(implementation = TimetableLectureModifyLegacyRequestDto::class))]),
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteTimetableLecture",
            responses = [ApiResponse(
                responseCode = "200",
                content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))]
            )]
        ),
    ),
)
annotation class TimetableDocs
