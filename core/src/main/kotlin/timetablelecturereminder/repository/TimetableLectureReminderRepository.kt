package com.wafflestudio.snutt.timetablelecturereminder.repository

import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TimetableLectureReminderRepository :
    CoroutineCrudRepository<TimetableLectureReminder, String>,
    TimetableLectureReminderCustomRepository {
    suspend fun findByTimetableLectureId(timetableLectureId: String): TimetableLectureReminder?

    suspend fun findByTimetableLectureIdIn(timetableLectureIds: List<String>): List<TimetableLectureReminder>

    suspend fun deleteByTimetableLectureId(timetableLectureId: String)
}
