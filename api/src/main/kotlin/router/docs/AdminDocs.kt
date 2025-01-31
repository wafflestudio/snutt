package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.clientconfig.dto.ConfigResponse
import com.wafflestudio.snutt.clientconfig.dto.PatchConfigRequest
import com.wafflestudio.snutt.clientconfig.dto.PostConfigRequest
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.storage.dto.FileUploadUriDto
import com.wafflestudio.snutt.popup.dto.PopupResponse
import com.wafflestudio.snutt.popup.dto.PostPopupRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import notification.dto.InsertNotificationRequest
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/admin/insert_noti",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "insertNotification",
                description = "어드민 권한으로 알림 보내기",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = InsertNotificationRequest::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                        required = true,
                        description = "userId null이면 모든 유저에게 보냄",
                    ),
                responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = OkResponse::class))])],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/configs/{name}",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "postConfig",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = PostConfigRequest::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                        required = true,
                    ),
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "name", required = true),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = ConfigResponse::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/configs/{name}",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getConfigs",
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "name", required = true),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = ConfigResponse::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/configs/{name}/{id}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "deleteConfig",
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "name", required = true),
                    Parameter(`in` = ParameterIn.PATH, name = "id", required = true),
                ],
                responses = [ApiResponse(responseCode = "200")],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/configs/{name}/{id}",
        method = [RequestMethod.PATCH],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "patchConfig",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = PatchConfigRequest::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                        required = true,
                    ),
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "name", required = true),
                    Parameter(`in` = ParameterIn.PATH, name = "id", required = true),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = ConfigResponse::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/images/{source}/upload-uris",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getUploadSignedUris",
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "source", required = true),
                    Parameter(`in` = ParameterIn.QUERY, name = "count", required = false),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = FileUploadUriDto::class)))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/popups",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "postPopup",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = PostPopupRequest::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                            ),
                        ],
                        required = true,
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = PopupResponse::class))],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/admin/popups/{id}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "deletePopup",
                parameters = [
                    Parameter(`in` = ParameterIn.PATH, name = "id", required = true),
                ],
                responses = [ApiResponse(responseCode = "200")],
            ),
    ),
)
annotation class AdminDocs
