package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.theme.dto.request.TimetableModifyThemeRequestDto
import com.wafflestudio.snutt.timetablelecturereminder.dto.TimetableLectureReminderDto
import com.wafflestudio.snutt.timetablelecturereminder.dto.request.TimetableLectureReminderModifyRequestDto
import com.wafflestudio.snutt.timetables.dto.TimetableLegacyDto
import com.wafflestudio.snutt.timetables.dto.request.CustomTimetableLectureAddLegacyRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableLectureModifyLegacyRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableModifyRequestDto
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
        path = "/v1/tables",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getBrief",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/recent",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getMostRecentlyUpdatedTimetables",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{year}/{semester}",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getTimetablesBySemester",
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "year", required = true),
                    Parameter(`in` = ParameterIn.PATH, name = "semester", required = true),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableLegacyDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "addTimetable",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = TimetableAddRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getTimetable",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "modifyTimetable",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = TimetableModifyRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "deleteTimetable",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/copy",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "copyTimetable",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableBriefDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/theme",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "modifyTimetableTheme",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = TimetableModifyThemeRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/primary",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "setPrimary",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = Unit::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/primary",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "unSetPrimary",
                parameters = [Parameter(`in` = ParameterIn.PATH, name = "timetableId", required = true)],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = Unit::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "addCustomLecture",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "isForced",
                        required = false,
                        description = "시간 겹치는 강의 강제로 삭제 후 실행",
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                ],
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = CustomTimetableLectureAddLegacyRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{lectureId}",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "addLecture",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "isForced",
                        required = false,
                        description = "시간 겹치는 강의 강제로 삭제 후 실행",
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "lectureId",
                        required = true,
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}/reset",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "resetTimetableLecture",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "isForced",
                        required = false,
                        description = "시간 겹치는 강의 강제로 삭제 후 실행",
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableLectureId",
                        required = true,
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "modifyTimetableLecture",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "isForced",
                        required = false,
                        description = "시간 겹치는 강의 강제로 삭제 후 실행",
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableLectureId",
                        required = true,
                    ),
                ],
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = TimetableLectureModifyLegacyRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "deleteTimetableLecture",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableLectureId",
                        required = true,
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLegacyDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}/reminder",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getReminder",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableLectureId",
                        required = true,
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLectureReminderDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/{timetableLectureId}/reminder",
        method = [RequestMethod.PUT],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "modifyReminder",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableLectureId",
                        required = true,
                    ),
                ],
                requestBody =
                    RequestBody(
                        required = true,
                        description = "강의 리마인더 설정 옵션 (NONE, TEN_MINUTES_BEFORE, ZERO_MINUTE, TEN_MINUTES_AFTER)",
                        content = [
                            Content(
                                schema = Schema(implementation = TimetableLectureReminderModifyRequestDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = TimetableLectureReminderDto::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/tables/{timetableId}/lecture/reminders",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getReminders",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "timetableId",
                        required = true,
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = TimetableLectureReminderDto::class)))],
                    ),
                ],
            ),
    ),
)
annotation class TimetableDocs
