package com.wafflestudio.snutt.lectures.service

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.exception.EvDataNotFoundException
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.evaluation.repository.SnuttEvRepository
import com.wafflestudio.snutt.lecturebuildings.service.LectureBuildingService
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.dto.LectureDto
import com.wafflestudio.snutt.lectures.dto.SearchDto
import com.wafflestudio.snutt.lectures.dto.placeInfos
import com.wafflestudio.snutt.lectures.repository.LectureRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

interface LectureService {
    fun findAll(): Flow<Lecture>

    suspend fun getByIdOrNull(lectureId: String): Lecture?

    suspend fun upsertLectures(lectures: Iterable<Lecture>)

    fun getLecturesByYearAndSemesterAsFlow(
        year: Int,
        semester: Semester,
    ): Flow<Lecture>

    suspend fun deleteLectures(lectures: Iterable<Lecture>)

    fun search(query: SearchDto): Flow<Lecture>

    suspend fun convertLecturesToLectureDtos(lectures: Iterable<Lecture>): List<LectureDto>

    suspend fun getEvSummary(lectureId: String): SnuttEvLectureSummaryDto
}

@Service
class LectureServiceImpl(
    private val lectureRepository: LectureRepository,
    private val snuttEvRepository: SnuttEvRepository,
    private val lectureBuildingService: LectureBuildingService,
) : LectureService {
    override fun findAll(): Flow<Lecture> = lectureRepository.findAll()

    override suspend fun getByIdOrNull(lectureId: String): Lecture? = lectureRepository.findById(lectureId)

    override fun getLecturesByYearAndSemesterAsFlow(
        year: Int,
        semester: Semester,
    ): Flow<Lecture> = lectureRepository.findAllByYearAndSemester(year, semester)

    override suspend fun upsertLectures(lectures: Iterable<Lecture>) = lectureRepository.saveAll(lectures).collect()

    override suspend fun deleteLectures(lectures: Iterable<Lecture>) = lectureRepository.deleteAll(lectures)

    override fun search(query: SearchDto): Flow<Lecture> = lectureRepository.searchLectures(query)

    override suspend fun convertLecturesToLectureDtos(lectures: Iterable<Lecture>): List<LectureDto> {
        val snuttIdToEvLectureMap =
            snuttEvRepository.getSummariesByIds(lectures.map { it.id!! }).associateBy { it.snuttId }
        return lectures.map { lecture ->
            val snuttEvLecture = snuttIdToEvLectureMap[lecture.id]
            LectureDto(lecture, snuttEvLecture)
        }.addLectureBuildings()
    }

    override suspend fun getEvSummary(lectureId: String): SnuttEvLectureSummaryDto {
        return snuttEvRepository.getSummariesByIds(listOf(lectureId)).firstOrNull() ?: throw EvDataNotFoundException
    }

    private suspend fun List<LectureDto>.addLectureBuildings(): List<LectureDto> =
        coroutineScope {
            val placeInfosAll =
                flatMap { it.classPlaceAndTimes.flatMap { classPlaceAndTime -> classPlaceAndTime.placeInfos } }.distinct()
            val buildings = lectureBuildingService.getLectureBuildings(placeInfosAll).associateBy { it.buildingNumber }
            forEach {
                it.classPlaceAndTimes.forEach { classPlaceAndTime ->
                    classPlaceAndTime.apply {
                        lectureBuildings = placeInfos.mapNotNull { placeInfo -> buildings[placeInfo.buildingNumber] }
                    }
                }
            }
            this@addLectureBuildings
        }
}
