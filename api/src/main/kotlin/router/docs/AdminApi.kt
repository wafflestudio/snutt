package com.wafflestudio.snu4t.router.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import notification.dto.InsertNotificationRequest
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@Bean
@RouterOperations(
    RouterOperation(
        path = "/v1/admin/insert-noti", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "insertNotification",
            requestBody = RequestBody(
                content = [Content(schema = Schema(implementation = InsertNotificationRequest::class))],
                required = true,
            ),
            responses = [ApiResponse(responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = Unit::class)))])]
        ),
    ),
)
annotation class AdminApi
