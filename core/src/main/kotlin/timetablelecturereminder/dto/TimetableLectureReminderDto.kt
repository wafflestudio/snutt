package com.wafflestudio.snutt.timetablelecturereminder.dto

import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder

data class TimetableLectureReminderDto(
    val id: String,
    val timetableLectureId: String,
    val offsetMinutes: Int,
)

fun TimetableLectureReminderDto(timetableLectureReminder: TimetableLectureReminder): TimetableLectureReminderDto =
    TimetableLectureReminderDto(
        id = timetableLectureReminder.id!!,
        timetableLectureId = timetableLectureReminder.timetableLectureId,
        offsetMinutes = timetableLectureReminder.offsetMinutes,
    )
