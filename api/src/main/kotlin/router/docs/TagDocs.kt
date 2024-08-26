package com.wafflestudio.snu4t.router.docs

import com.wafflestudio.snu4t.tag.data.TagListResponse
import com.wafflestudio.snu4t.tag.data.TagListUpdateTimeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations

@RouterOperations(
    RouterOperation(
        path = "/v1/tags/{year}/{semester}",
        operation = Operation(
            operationId = "getTagList",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "year", required = true),
                Parameter(`in` = ParameterIn.PATH, name = "semester", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TagListResponse::class))])]
        ),
    ),
    RouterOperation(
        path = "/v1/tags/{year}/{semester}/update_time",
        operation = Operation(
            operationId = "getTagListUpdateTime",
            parameters = [
                Parameter(`in` = ParameterIn.PATH, name = "year", required = true),
                Parameter(`in` = ParameterIn.PATH, name = "semester", required = true)
            ],
            responses = [ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TagListUpdateTimeResponse::class))])]
        )
    ),

)
annotation class TagDocs
