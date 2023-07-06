package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.lectures.dto.LectureDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/vacancy-notifications/lectures", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getVacancyNotificationLectures",
            responses = [
                ApiResponse(responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = LectureDto::class)))]),
                ApiResponse(
                    responseCode = "400",
                    description = "이전 학기 강의 등록 시",
                    content = [Content(examples = [ExampleObject(value = """{"errcode" : "40005", "message" : "이전 학기에는 빈자리 알림을 등록할 수 없습니다.", "displayMessage" : "이전 학기에는 빈자리 알림을 등록할 수 없습니다."}""")])]
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "빈자리 알림 중복",
                    content = [Content(examples = [ExampleObject(value = """{"errcode" : "40900", "message" : "빈자리 알림 중복", "displayMessage" : "빈자리 알림 중복"}""")])]
                ),
                ApiResponse(
                    responseCode = "404",
                    content = [Content(examples = [ExampleObject(value = """{"errcode" : "16387", "message" : "lecture가 없습니다.", "displayMessage" : "lecture가 없습니다."}""")])]
                ),
            ]
        ),
    ),
    RouterOperation(
        path = "/v1/vacancy-notifications/lectures/{lectureId}", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addVacancyNotification",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "lectureId", required = true),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
    RouterOperation(
        path = "/v1/vacancy-notifications/lectures/{lectureId}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteVacancyNotification",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "lectureId", required = true),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
)
annotation class VacancyNotificationDocs
