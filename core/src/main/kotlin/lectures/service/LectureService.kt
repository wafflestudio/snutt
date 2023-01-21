package com.wafflestudio.snu4t.lectures.service

import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.repository.LectureRepository
import org.springframework.stereotype.Service

interface LectureService {
    suspend fun getByIdOrNull(lectureId: String): Lecture?
}

@Service
class LectureServiceImpl(private val lectureRepository: LectureRepository) : LectureService {
    override suspend fun getByIdOrNull(lectureId: String): Lecture? = lectureRepository.findById(lectureId)
}
