package com.wafflestudio.snutt.lecturebuildings.repository

import com.wafflestudio.snutt.lecturebuildings.data.LectureBuilding
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LectureBuildingRepository : CoroutineCrudRepository<LectureBuilding, String> {
    fun findByBuildingNumberIn(buildingNumbers: List<String>): Flow<LectureBuilding>
}
