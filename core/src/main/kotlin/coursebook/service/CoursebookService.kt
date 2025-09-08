package com.wafflestudio.snutt.coursebook.service

import com.wafflestudio.snutt.coursebook.data.Coursebook
import com.wafflestudio.snutt.coursebook.repository.CoursebookRepository
import org.springframework.stereotype.Service

interface CoursebookService {
    suspend fun getLatestCoursebook(): Coursebook

    suspend fun getCoursebooks(): List<Coursebook>

    suspend fun getLastTwoCourseBooksBeforeCurrent(): List<Coursebook>
}

@Service
class CoursebookServiceImpl(
    private val coursebookRepository: CoursebookRepository,
) : CoursebookService {
    override suspend fun getLatestCoursebook(): Coursebook = coursebookRepository.findFirstByOrderByYearDescSemesterDesc()

    override suspend fun getCoursebooks(): List<Coursebook> = coursebookRepository.findAllByOrderByYearDescSemesterDesc()

    override suspend fun getLastTwoCourseBooksBeforeCurrent(): List<Coursebook> =
        coursebookRepository.findTop3ByOrderByYearDescSemesterDesc().slice(
            1..2,
        )
}
