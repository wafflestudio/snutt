package com.wafflestudio.snu4t.lecturebuildings.repository

import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureBuildingRepository : CoroutineCrudRepository<LectureBuilding, String> {
    fun findByBuildingNumberIn(buildingNumbers: List<String>): Flow<LectureBuilding>
}
