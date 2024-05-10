package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.evaluation.repository.SnuttEvRepository
import com.wafflestudio.snu4t.lecturebuildings.service.LectureBuildingService
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.dto.LectureDto
import com.wafflestudio.snu4t.lectures.dto.SearchDto
import com.wafflestudio.snu4t.lectures.dto.placeInfos
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

interface LectureService {
    fun findAll(): Flow<Lecture>
    suspend fun getByIdOrNull(lectureId: String): Lecture?
    suspend fun upsertLectures(lectures: Iterable<Lecture>)
    fun getLecturesByYearAndSemesterAsFlow(year: Int, semester: Semester): Flow<Lecture>
    suspend fun deleteLectures(lectures: Iterable<Lecture>)
    fun search(query: SearchDto): Flow<Lecture>
    suspend fun convertLecturesToLectureDtos(lectures: Iterable<Lecture>): List<LectureDto>
}

@Service
class LectureServiceImpl(
    private val lectureRepository: LectureRepository,
    private val snuttEvRepository: SnuttEvRepository,
    private val lectureBuildingService: LectureBuildingService,
) : LectureService {
    override fun findAll(): Flow<Lecture> = lectureRepository.findAll()

    override suspend fun getByIdOrNull(lectureId: String): Lecture? = lectureRepository.findById(lectureId)
    override fun getLecturesByYearAndSemesterAsFlow(year: Int, semester: Semester): Flow<Lecture> =
        lectureRepository.findAllByYearAndSemester(year, semester)

    override suspend fun upsertLectures(lectures: Iterable<Lecture>) =
        lectureRepository.saveAll(lectures).collect()

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
    private suspend fun List<LectureDto>.addLectureBuildings(): List<LectureDto> = coroutineScope {
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
