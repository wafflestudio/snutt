package com.wafflestudio.snu4t.lecturebuildings.service

import com.wafflestudio.snu4t.lecturebuildings.data.Campus
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuildingUpdateResult
import com.wafflestudio.snu4t.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snu4t.lecturebuildings.repository.LectureBuildingRepository
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.Instant

interface LectureBuildingPopulateService {
    suspend fun populateLectureBuildingsWithFetch(lectures: List<Lecture>): LectureBuildingUpdateResult
    suspend fun populateLectureBuildingsOfTimetables(lecture: Lecture): List<Timetable>
}

@Service
class LectureBuildingPopulateServiceImpl(
    private val lectureBuildingFetchService: LectureBuildingFetchService,
    private val lectureRepository: LectureRepository,
    private val lectureBuildingRepository: LectureBuildingRepository,
    private val timetableRepository: TimetableRepository,
) : LectureBuildingPopulateService {
    // 빌딩 정보를 조회하고 없으면 크롤링해옴

    override suspend fun populateLectureBuildingsWithFetch(lectures: List<Lecture>): LectureBuildingUpdateResult {
        val lecturePlaceInfos = lectures.flatMap { it.classPlaceAndTimes }
            .map { it.place }.distinct()
            .flatMap { PlaceInfo.getValuesOf(it) }

        val lectureBuildingNumbers = lecturePlaceInfos.map { it.buildingNumber }.toSet()
        val lecturePlaceInfoMap = lecturePlaceInfos.associateBy { it.rawString }

        val savedLectureBuildings = lectureBuildingRepository.findByBuildingNumberIsIn(lectureBuildingNumbers)
        val savedBuildingNumbers = savedLectureBuildings.map { it.buildingNumber }.toSet()

        // 존재하지 않는 강의동을 캠퍼스맵에서 긁어와서 저장
        val buildingNumbersToFetch = lectureBuildingNumbers - savedBuildingNumbers
        val fetchedBuildlingInfo = fetchBuildings(buildingNumbersToFetch)
        val fetchedLectureBuildings = lectureBuildingRepository.saveAll(fetchedBuildlingInfo).toList()

        // Lecture에 강의동 정보를 추가
        val updateResult = updateBuildingInfoOfLectures(lectures, lecturePlaceInfoMap, savedLectureBuildings + fetchedLectureBuildings)
        val results = lectureRepository.saveAll(updateResult.lecturesWithBuildingInfos).toList()

        return updateResult
    }

    private suspend fun fetchBuildings(buildingNumbers: Set<String>): List<LectureBuilding> = runBlocking {
        return@runBlocking buildingNumbers.map {
            async {
                return@async lectureBuildingFetchService.getSnuMapLectureBuilding(
                    Campus.GWANAK,
                    it
                )
            }.await()
        }
    }.filterNotNull()

    private fun updateBuildingInfoOfLectures(
        lectures: List<Lecture>,
        placeInfoMap: Map<String, PlaceInfo>,
        lectureBuildings: List<LectureBuilding>
    ): LectureBuildingUpdateResult {
        val lectureBuildingMap = lectureBuildings.associateBy { it.buildingNumber }

        val infoPopulated: MutableList<Lecture> = mutableListOf()
        val infoEmpty: MutableList<Lecture> = mutableListOf()
        val infoPopulationFailed: MutableList<Lecture> = mutableListOf()

        for (lecture in lectures) {
            if (lecture.classPlaceAndTimes.isEmpty() ||
                lecture.classPlaceAndTimes.map { it.place }.joinToString("").isBlank()
            ) {
                infoEmpty.add(lecture)
                continue
            }

            val newClassPlaceAndTimes = lecture.classPlaceAndTimes.map {
                it.apply {
                    this.lectureBuildings = PlaceInfo.getValuesOf(it.place)
                        .mapNotNull { placeInfoMap[it.rawString] }
                        .distinctBy { it.buildingNumber }
                        .mapNotNull { lectureBuildingMap[it.buildingNumber] }
                }
            }

            val succeeded = newClassPlaceAndTimes.map { it.place.isBlank() || it.lectureBuildings != null }
                .reduce { acc, b -> acc && b }

            if (succeeded) {
                infoPopulated.add(lecture)
            } else {
                infoPopulationFailed.add(lecture)
            }
        }

        return LectureBuildingUpdateResult(
            lecturesWithBuildingInfos = infoPopulated,
            lecturesWithOutBuildingInfos = infoEmpty,
            lecturesFailed = infoPopulationFailed
        )
    }

    override suspend fun populateLectureBuildingsOfTimetables(lecture: Lecture): List<Timetable> {
        val updatedTimetables = timetableRepository.findAllContainsLectureId(lecture.year, lecture.semester, lecture.id!!)
            .map { timetable ->
                timetable.apply {
                    lectures.find { it.lectureId == lecture.id && it.classPlaceAndTimes == lecture.classPlaceAndTimes }?.apply {
                        classPlaceAndTimes = lecture.classPlaceAndTimes
                    }
                    updatedAt = Instant.now()
                }
            }
            .toList()

        return timetableRepository.saveAll(updatedTimetables).toList()
    }
}
