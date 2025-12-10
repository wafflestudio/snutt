package com.wafflestudio.snutt.timetables.repository

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.timetables.data.Timetable
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TimetableRepository :
    CoroutineCrudRepository<Timetable, String>,
    TimetableCustomRepository {
    fun findAllByUserId(userId: String): Flow<Timetable>

    fun findAllByUserIdAndIsPrimaryTrue(userId: String): Flow<Timetable>

    fun findAllByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
    ): Flow<Timetable>

    suspend fun findByUserIdAndId(
        userId: String,
        id: String,
    ): Timetable?

    suspend fun findFirstByUserIdOrderByUpdatedAtDesc(userId: String): Timetable?

    fun findByUserIdAndYearAndSemester(
        userId: String,
        year: Int,
        semester: Semester,
    ): Flow<Timetable>

    suspend fun findByUserIdAndYearAndSemesterAndIsPrimaryTrue(
        userId: String,
        year: Int,
        semester: Semester,
    ): Timetable?

    suspend fun existsByUserIdAndYearAndSemesterAndTitle(
        userId: String,
        year: Int,
        semester: Semester,
        title: String,
    ): Boolean

    suspend fun countAllByUserId(userId: String): Long

    suspend fun findByUserIdAndThemeId(
        userId: String,
        themeId: String,
    ): List<Timetable>

    suspend fun countAllByIsPrimaryTrueAndYearAndSemester(
        year: Int,
        semester: Semester,
    ): Long
}
