package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.timetables.data.Timetable
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TimetableRepository : CoroutineCrudRepository<Timetable, String>, TimetableCustomRepository {
    fun findAllByUserId(userId: String): Flow<Timetable>
    fun findByUserIdAndYearAndSemester(userId: String, year: Int, semester: Semester): Flow<Timetable>
    suspend fun findByUserIdAndYearAndSemesterAndIsPrimaryTrue(userId: String, year: Int, semester: Semester): Timetable?
    suspend fun existsByUserIdAndYearAndSemesterAndTitle(userId: String, year: Int, semester: Semester, title: String): Boolean
}
