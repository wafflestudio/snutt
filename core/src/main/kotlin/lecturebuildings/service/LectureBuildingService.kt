package com.wafflestudio.snu4t.lecturebuildings.service

import com.wafflestudio.snu4t.lecturebuildings.api.SnuMapRepository
import com.wafflestudio.snu4t.lecturebuildings.data.Campus
import com.wafflestudio.snu4t.lecturebuildings.data.GeoCoordinate
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding
import com.wafflestudio.snu4t.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snu4t.lecturebuildings.data.SnuMapSearchItem
import com.wafflestudio.snu4t.lecturebuildings.data.SnuMapSearchResult
import com.wafflestudio.snu4t.lecturebuildings.repository.LectureBuildingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface LectureBuildingService {
    suspend fun getLectureBuildings(placeInfos: List<PlaceInfo>): List<LectureBuilding>
    suspend fun updateLectureBuildings(placeInfos: List<PlaceInfo>)
}

@Service
class LectureBuildingServiceImpl(
    private val snuMapRepository: SnuMapRepository,
    private val lectureBuildingRepository: LectureBuildingRepository
) : LectureBuildingService {
    override suspend fun getLectureBuildings(placeInfos: List<PlaceInfo>): List<LectureBuilding> {
        val buildingNumbers = placeInfos.filter { it.campus == Campus.GWANAK }.map { it.buildingNumber }
        return lectureBuildingRepository.findByBuildingNumberIn(buildingNumbers).toList()
    }

    override suspend fun updateLectureBuildings(placeInfos: List<PlaceInfo>) {
        coroutineScope {
            placeInfos.filter { it.campus == Campus.GWANAK }.map {
                async {
                    snuMapRepository.getLectureBuildingSearchResult(it.buildingNumber)
                        .getMostProbableSearchItem(it.buildingNumber)
                }
            }.awaitAll().filterNotNull().map {
                LectureBuilding(
                    buildingNumber = it.buildingNumber ?: "",
                    buildingNameKor = it.name,
                    buildingNameEng = it.englishName ?: "",
                    locationInDMS = GeoCoordinate(it.latitudeInDMS, it.longitudeInDMS),
                    locationInDecimal = GeoCoordinate(it.latitudeInDecimal, it.longitudeInDecimal),
                    campus = Campus.GWANAK
                )
            }
        }.let { lectureBuildingRepository.saveAll(it) }
    }

    private fun SnuMapSearchResult.getMostProbableSearchItem(buildingNumber: String): SnuMapSearchItem? = searchList
        .filter { it.contentType == "F" && it.facType == "OTHER" && it.buildingNumber == buildingNumber }
        .minByOrNull { it.name.count() }
}
