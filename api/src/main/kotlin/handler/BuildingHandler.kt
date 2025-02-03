package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snutt.lecturebuildings.service.LectureBuildingService
import com.wafflestudio.snutt.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class BuildingHandler(
    private val lectureBuildingService: LectureBuildingService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiNoAuthMiddleware,
    ) {
    suspend fun searchBuildings(req: ServerRequest): ServerResponse =
        handle(req) {
            val placesQuery =
                req.parseRequiredQueryParam<String>("places")
                    .split(",")
                    .flatMap { PlaceInfo.getValuesOf(it) }
                    .distinct()
            val buildings = lectureBuildingService.getLectureBuildings(placesQuery)
            ListResponse(buildings)
        }
}
