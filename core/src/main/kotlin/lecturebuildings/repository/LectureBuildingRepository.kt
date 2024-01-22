package com.wafflestudio.snu4t.lecturebuildings.repository

import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureBuildingRepository : CoroutineCrudRepository<LectureBuilding, String> {
    suspend fun findByBuildingNumber(buildingNumber: String): LectureBuilding?
    suspend fun findByBuildingNumberIsIn(buildingNumbers: List<String>): List<LectureBuilding>
}
