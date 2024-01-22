package com.wafflestudio.snu4t.lecturebuildings.service

import com.wafflestudio.snu4t.lecturebuildings.data.Campus
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuildingUpdateResult
import com.wafflestudio.snu4t.lecturebuildings.repository.LectureBuildingRepository
import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

interface LectureBuildingPopulateService {
    suspend fun populateLectureBuildings(lecture: Lecture): LectureBuildingUpdateResult
}

@Service
class LectureBuildingPopulateServiceImpl(
    private val lectureBuildingFetchService: LectureBuildingFetchService,
    private val lectureRepository: LectureRepository,
    private val lectureBuildingRepository: LectureBuildingRepository
) : LectureBuildingPopulateService {
    override suspend fun populateLectureBuildings(lecture: Lecture): LectureBuildingUpdateResult {
        if (lecture.classPlaceAndTimes.isEmpty()) {
            return LectureBuildingUpdateResult(lecture, emptyList())
        }

        val placeBuildingInfos = lecture.classPlaceAndTimes
            .mapNotNull { parseBuildingNumberAndCampusFromPlace(it.place) }
            .distinctBy { it.first }
        val placeBuildingNumbers = placeBuildingInfos.map { it.first }

        val lectureBuildings = lectureBuildingRepository.findByBuildingNumberIsIn(placeBuildingNumbers).toMutableList()
        val savedLectureBuildingNumbers = lectureBuildings.map { it.buildingNumber }

        val buildingsToFetch = placeBuildingInfos.filter { !savedLectureBuildingNumbers.contains(it.first) }
        val fetchedBuildings = runBlocking {
            return@runBlocking buildingsToFetch.map {
                async { return@async lectureBuildingFetchService.getSnuMapLectureBuilding(it.second, it.first) }
            }.awaitAll()
        }.filterNotNull()

        lectureBuildings.addAll(fetchedBuildings)

        val classPlaceAndTimesWithBuildings = lecture.classPlaceAndTimes.map {
            ClassPlaceAndTime(
                day = it.day,
                place = it.place,
                startMinute = it.startMinute,
                endMinute = it.endMinute,
                lectureBuilding = lectureBuildings.firstOrNull {
                    building ->
                    building.buildingNumber == parseBuildingNumberAndStripCampusPrefix(it.place)
                }
            )
        }

        return LectureBuildingUpdateResult(
            lectureRepository.save(
                lecture.apply {
                    this.classPlaceAndTimes = classPlaceAndTimesWithBuildings
                }
            ),
            buildingsAdded = fetchedBuildings
        )
    }

    private fun parseBuildingNumberAndCampusFromPlace(place: String): Pair<String, Campus>? = place
        .let { it.split("-").dropLast(1).joinToString("-") }
        .ifBlank { null }
        ?.let {
            when (it.first()) {
                '#' -> Pair(it.removePrefix("#"), Campus.YEONGEON)
                '*' -> Pair(it.removePrefix("*"), Campus.PYEONGCHANG)
                else -> Pair(it, Campus.GWANAK)
            }
        }

    private fun parseBuildingNumberAndStripCampusPrefix(place: String): String? = place
        .split("-").dropLast(1).joinToString("-")
        .removePrefix("#").removePrefix("*")
        .ifBlank { null }
}
