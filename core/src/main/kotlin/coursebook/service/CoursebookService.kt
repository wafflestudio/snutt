package com.wafflestudio.snu4t.coursebook.service

import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.coursebook.repository.CoursebookRepository
import org.springframework.stereotype.Service

interface CoursebookService {
    suspend fun getLatestCoursebook(): Coursebook
}

@Service
class CoursebookServiceImpl(private val coursebookRepository: CoursebookRepository) : CoursebookService {
    override suspend fun getLatestCoursebook(): Coursebook =
        coursebookRepository.findFirstByOrderByYearDescSemesterDesc()
}
