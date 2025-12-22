package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.popup.dto.PopupResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/popups",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getPopups",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = PopupsResponse::class))],
                    ),
                ],
            ),
    ),
)
annotation class PopupDocs

class PopupsResponse : ListResponse<PopupResponse>(listOf())
