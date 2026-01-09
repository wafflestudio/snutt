package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.filter.SnuttNoAuthApiFilterTarget
import com.wafflestudio.snutt.lecturebuildings.data.LectureBuilding
import com.wafflestudio.snutt.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snutt.lecturebuildings.service.LectureBuildingService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttNoAuthApiFilterTarget
@RequestMapping(
    "/v1/buildings",
    "/buildings",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class BuildingController(
    private val lectureBuildingService: LectureBuildingService,
) {
    @GetMapping("")
    suspend fun searchBuildings(
        @RequestParam places: String,
    ): ListResponse<LectureBuilding> {
        val placesQuery =
            places
                .split(",")
                .flatMap { PlaceInfo.getValuesOf(it) }
                .distinct()
        val buildings = lectureBuildingService.getLectureBuildings(placesQuery)
        return ListResponse(buildings)
    }
}
