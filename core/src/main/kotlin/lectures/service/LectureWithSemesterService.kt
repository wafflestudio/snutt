package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.lectures.data.LectureWithSemester
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import org.springframework.stereotype.Service

interface LectureWithSemesterService {
    suspend fun getByIdOrNull(lectureId: String): LectureWithSemester?
}

@Service
class LectureWithSemesterServiceImpl(private val lectureRepository: LectureRepository) : LectureWithSemesterService {
    override suspend fun getByIdOrNull(lectureId: String): LectureWithSemester? = lectureRepository.findById(lectureId)
}
