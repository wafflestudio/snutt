package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

interface LectureService {
    fun findAll(): Flow<Lecture>
    suspend fun getByIdOrNull(lectureId: String): Lecture?
    suspend fun upsertLectures(lectures: Iterable<Lecture>)
    fun getLecturesByYearAndSemesterAsFlow(year: Int, semester: Semester): Flow<Lecture>
    suspend fun deleteLectures(lectures: Iterable<Lecture>)
}

@Service
class LectureServiceImpl(private val lectureRepository: LectureRepository) : LectureService {
    override fun findAll(): Flow<Lecture> = lectureRepository.findAll()

    override suspend fun getByIdOrNull(lectureId: String): Lecture? = lectureRepository.findById(lectureId)
    override fun getLecturesByYearAndSemesterAsFlow(year: Int, semester: Semester): Flow<Lecture> =
        lectureRepository.findAllByYearAndSemester(year, semester)

    override suspend fun upsertLectures(lectures: Iterable<Lecture>) =
        lectureRepository.saveAll(lectures).collect()

    override suspend fun deleteLectures(lectures: Iterable<Lecture>) = lectureRepository.deleteAll(lectures)
}
