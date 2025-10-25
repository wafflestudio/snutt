package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiaryDailyClassType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DiaryDailyClassTypeRepository : CoroutineCrudRepository<DiaryDailyClassType, String> {
    suspend fun findAllByActiveTrue(): List<DiaryDailyClassType>

    suspend fun findByName(name: String): DiaryDailyClassType?

    suspend fun findAllByNameIn(names: List<String>): List<DiaryDailyClassType>
}
