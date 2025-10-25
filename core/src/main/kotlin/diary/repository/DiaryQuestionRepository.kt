package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiaryQuestion
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DiaryQuestionRepository : CoroutineCrudRepository<DiaryQuestion, String> {
    suspend fun findByTargetDailyClassTypeIdsInAndActiveTrue(targetDailyClassTypeIds: List<String>): List<DiaryQuestion>

    suspend fun findAllByActiveTrue(): List<DiaryQuestion>

    suspend fun findAllByIdIn(ids: List<String>): List<DiaryQuestion>

    suspend fun countByIdIn(ids: List<String>): Int
}
