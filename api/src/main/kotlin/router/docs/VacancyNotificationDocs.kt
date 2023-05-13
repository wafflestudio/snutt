package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.vacancynotification.dto.VacancyNotificationDto
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

@RouterOperations(
    RouterOperation(
        path = "/v1/vacancy-notifications/lectures/{lectureId}", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "addVacancyNotification",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "lectureId", required = true),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = VacancyNotificationDto::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/vacancy-notifications/{id}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "deleteVacancyNotification",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "id", required = true),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema())])]
        ),
    ),
)
annotation class VacancyNotificationDocs
