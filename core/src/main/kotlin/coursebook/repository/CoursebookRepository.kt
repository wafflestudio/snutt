package com.wafflestudio.snutt.coursebook.repository

import com.wafflestudio.snutt.coursebook.data.Coursebook
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CoursebookRepository : CoroutineCrudRepository<Coursebook, String> {
    suspend fun findFirstByOrderByYearDescSemesterDesc(): Coursebook

    suspend fun findAllByOrderByYearDescSemesterDesc(): List<Coursebook>

    suspend fun findTop3ByOrderByYearDescSemesterDesc(): List<Coursebook>
}
