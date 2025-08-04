package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiaryActivity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DiaryActivityRepository : CoroutineCrudRepository<DiaryActivity, String> {
    suspend fun findAllByActiveTrue(): List<DiaryActivity>

    suspend fun findByName(name: String): DiaryActivity?

    suspend fun findByNameIn(names: List<String>): List<DiaryActivity>
}
