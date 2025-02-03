package com.wafflestudio.snutt.router.docs

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.lecturebuildings.data.LectureBuilding
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
        path = "/v1/buildings",
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        operation =
            Operation(
                operationId = "searchBuildings",
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "places",
                        required = true,
                        description = """
                    Comma separated list of place codes.
                    custom 강의 제외하는 것을 추천
                """,
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        content = [Content(schema = Schema(implementation = BuildingsResponse::class))],
                    ),
                ],
            ),
    ),
)
annotation class BuildingsDocs

private class BuildingsResponse : ListResponse<LectureBuilding>(listOf())
