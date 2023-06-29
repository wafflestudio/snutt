package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.common.dto.OkResponse
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
        path = "/v1/admin/insert_noti", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "insertNotification",
            description = "어드민 권한으로 알림 보내기, 구버전 api 라서 snake case 사용",
            requestBody = RequestBody(
                content = [Content(schema = Schema(implementation = InsertNotificationRequest::class))],
                required = true,
            ),
            responses = [ApiResponse(responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = OkResponse::class)))])]
        ),
    ),
)
annotation class AdminDocs
