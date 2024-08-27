package com.wafflestudio.snu4t.coursebook.repository

import com.wafflestudio.snu4t.coursebook.data.Coursebook
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CoursebookRepository : CoroutineCrudRepository<Coursebook, String> {
    suspend fun findFirstByOrderByYearDescSemesterDesc(): Coursebook

    fun findAllByOrderByYearDescSemesterDesc(): Flow<Coursebook>
}
