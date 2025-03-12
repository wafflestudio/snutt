package com.wafflestudio.snutt.lectures.service

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.evaluation.service.EvService
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.lectures.dto.LectureDto
import com.wafflestudio.snutt.lectures.dto.SearchDto
import com.wafflestudio.snutt.lectures.repository.LectureRepository
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
}

@Service
class LectureServiceImpl(
    private val lectureRepository: LectureRepository,
    private val snuttEvService: EvService,
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
            snuttEvService.getSummariesByIds(lectures.map { it.id!! }).associateBy { it.snuttId }
        return lectures.map { lecture ->
            val snuttEvLecture = snuttIdToEvLectureMap[lecture.id]
            LectureDto(lecture, snuttEvLecture)
        }
    }
}
