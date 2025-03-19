package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.notification.dto.PushPreferenceDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod

@RouterOperations(
    RouterOperation(
        path = "/v1/push/preferences",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "getPushPreferences",
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [
                            Content(
                                schema = Schema(implementation = PushPreferenceDto::class),
                                examples = [
                                    ExampleObject(
                                        value = """
                                            {
                                              "pushPreferences": [
                                                {
                                                    "type": "LECTURE_UPDATE",
                                                    "isEnabled": "true"
                                                },
                                                {
                                                    "type": "VACANCY_NOTIFICATION",
                                                    "isEnabled": "true"
                                                }
                                              ]
                                            }
                                            """,
                                    ),
                                ],
                            ),
                        ],
                    ),
                ],
            ),
    ),
    RouterOperation(
        path = "/v1/push/preferences",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "savePushPreferences",
                requestBody =
                    RequestBody(
                        content = [
                            Content(
                                schema = Schema(implementation = PushPreferenceDto::class),
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples = [
                                    ExampleObject(
                                        value = """
                                            {
                                                "pushPreferences": [
                                                    {
                                                        "type": "LECTURE_UPDATE",
                                                        "isEnabled": false
                                                    },
                                                    {
                                                        "type": "VACANCY_NOTIFICATION",
                                                        "isEnabled": false
                                                    }
                                                ]
                                            }
                                        """,
                                    ),
                                ],
                            ),
                        ],
                    ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                    ),
                ],
            ),
    ),
)
annotation class PushDocs()
