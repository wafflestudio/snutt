package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.ListResponse
import com.wafflestudio.snu4t.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snu4t.lecturebuildings.service.LectureBuildingService
import com.wafflestudio.snu4t.middleware.SnuttRestApiNoAuthMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class BuildingHandler (
    private val lectureBuildingService: LectureBuildingService,
    snuttRestApiNoAuthMiddleware: SnuttRestApiNoAuthMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiNoAuthMiddleware
) {
    suspend fun searchBuildings(req: ServerRequest): ServerResponse = handle(req) {
        val placesQuery = req.parseRequiredQueryParam<String>("places").split(",").flatMap { PlaceInfo.getValuesOf(it) }
        val buildings = lectureBuildingService.getLectureBuildings(placesQuery)
        ListResponse(buildings)
    }
}
