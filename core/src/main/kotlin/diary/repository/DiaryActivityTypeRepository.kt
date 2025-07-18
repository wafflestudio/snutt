package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiaryActivityType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DiaryActivityTypeRepository : CoroutineCrudRepository<DiaryActivityType, String> {
    suspend fun findAllByActiveTrue(): List<DiaryActivityType>

    suspend fun findByName(name: String): DiaryActivityType?

    suspend fun findByNameIn(names: List<String>): List<DiaryActivityType>
}
