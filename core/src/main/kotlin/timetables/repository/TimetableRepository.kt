package com.wafflestudio.snu4t.timetables.repository

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.data.TimetableLecture
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TimetableRepository : CoroutineCrudRepository<Timetable, String>, TimetableCustomRepository {
    fun findAllByUserId(userId: String): Flow<Timetable>
    suspend fun findAllByUserIdAndYearAndSemesterAndTitle(userId: String, year: Int, semester: Semester, title: String): List<Timetable>
}
