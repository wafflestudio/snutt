package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.evaluation.repository.SnuttEvRepository
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.dto.LectureDto
import com.wafflestudio.snu4t.lectures.dto.SearchDto
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface LectureService {
    fun findAll(): Flow<Lecture>
    suspend fun getByIdOrNull(lectureId: String): Lecture?
    suspend fun upsertLectures(lectures: Iterable<Lecture>)
    fun getLecturesByYearAndSemesterAsFlow(year: Int, semester: Semester): Flow<Lecture>
    suspend fun deleteLectures(lectures: Iterable<Lecture>)
    suspend fun search(query: SearchDto): List<LectureDto>
}

@Service
class LectureServiceImpl(
    private val lectureRepository: LectureRepository,
    private val snuttEvRepository: SnuttEvRepository
) : LectureService {
    override fun findAll(): Flow<Lecture> = lectureRepository.findAll()

    override suspend fun getByIdOrNull(lectureId: String): Lecture? = lectureRepository.findById(lectureId)
    override fun getLecturesByYearAndSemesterAsFlow(year: Int, semester: Semester): Flow<Lecture> =
        lectureRepository.findAllByYearAndSemester(year, semester)

    override suspend fun upsertLectures(lectures: Iterable<Lecture>) =
        lectureRepository.saveAll(lectures).collect()

    override suspend fun deleteLectures(lectures: Iterable<Lecture>) = lectureRepository.deleteAll(lectures)
    override suspend fun search(query: SearchDto): List<LectureDto> {
        val lectures = lectureRepository.searchLectures(query).toList()
        val snuttEvLectures =
            snuttEvRepository.getSummariesByIds(lectures.map { it.id!! }).associateBy { it.snuttId }
        return lectures.map { lecture ->
            val snuttEvLecture = snuttEvLectures[lecture.id]
            LectureDto(lecture, snuttEvLecture)
        }
    }
}
