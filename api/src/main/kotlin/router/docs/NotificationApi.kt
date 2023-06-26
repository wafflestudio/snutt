package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.notification.dto.NotificationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@Bean
@RouterOperations(
    RouterOperation(
        path = "/v1/notification", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getNotification",
            parameters = [
                Parameter(`in` = ParameterIn.QUERY, name = "offset", required = false),
                Parameter(`in` = ParameterIn.QUERY, name = "limit", required = false),
                Parameter(`in` = ParameterIn.QUERY, name = "explicit", required = false),
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = NotificationResponse::class)))])]
        ),
    ),
    RouterOperation(
        path = "/v1/notification/count", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE],
        operation = Operation(
            operationId = "getUnreadCounts",
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = Long::class))])]
        ),
    )
)
annotation class NotificationApi
