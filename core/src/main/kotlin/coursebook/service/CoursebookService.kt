package com.wafflestudio.snu4t.coursebook.service

import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.coursebook.repository.CoursebookRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface CoursebookService {
    suspend fun getLatestCoursebook(): Coursebook

    suspend fun getCourseBooks(): List<Coursebook>
}

@Service
class CoursebookServiceImpl(private val coursebookRepository: CoursebookRepository) : CoursebookService {
    override suspend fun getLatestCoursebook(): Coursebook =
        coursebookRepository.findFirstByOrderByYearDescSemesterDesc()

    override suspend fun getCourseBooks(): List<Coursebook> = coursebookRepository.findAllByOrderByYearDescSemesterDesc().toList()
}
