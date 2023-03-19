package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.users.dto.LocalRegisterRequest
import com.wafflestudio.snu4t.users.dto.RegisterResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperation(
    path = "/v1/register_local", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE],
    operation = Operation(
        operationId = "registerLocal",
        parameters = [Parameter(`in` = ParameterIn.HEADER, name = "userId", required = true)],
        requestBody = RequestBody(content = [Content(schema = Schema(implementation = LocalRegisterRequest::class))]),
        responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = RegisterResponse::class))])]
    ),
)
annotation class AuthDocs
