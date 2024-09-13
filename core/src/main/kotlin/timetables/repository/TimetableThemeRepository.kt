package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TimetableThemeRepository : CoroutineCrudRepository<TimetableTheme, String>, TimetableThemeCustomRepository {
    suspend fun findByIdAndUserId(
        id: String,
        userId: String,
    ): TimetableTheme?

    suspend fun findByUserIdAndIsCustomTrueOrderByUpdatedAtDesc(userId: String): List<TimetableTheme>

    suspend fun existsByUserIdAndName(
        userId: String,
        name: String,
    ): Boolean
}
