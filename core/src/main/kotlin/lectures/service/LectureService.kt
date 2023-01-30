package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface LectureService {
    suspend fun getByIdOrNull(lectureId: String): Lecture?
    suspend fun getByYearAndSemseter(year: Int, semester: Semester): List<Lecture>
    suspend fun insertLectures(lectures: Iterable<Lecture>): List<Lecture>
    suspend fun upsertLectures(lectures: Iterable<Lecture>): List<Lecture>
}

@Service
class LectureServiceImpl(private val lectureRepository: LectureRepository) : LectureService {
    override suspend fun getByIdOrNull(lectureId: String): Lecture? = lectureRepository.findById(lectureId)
    override suspend fun getByYearAndSemseter(year: Int, semester: Semester): List<Lecture> =
        lectureRepository.findAllByYearAndSemester(year, semester).toList()

    override suspend fun insertLectures(lectures: Iterable<Lecture>): List<Lecture> =
        lectureRepository.saveAll(lectures).toList()

    override suspend fun upsertLectures(lectures: Iterable<Lecture>): List<Lecture> {
        return lectureRepository.upsertLectures(lectures)
    }
}
