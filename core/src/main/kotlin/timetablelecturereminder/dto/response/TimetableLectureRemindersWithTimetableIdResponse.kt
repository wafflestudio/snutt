package com.wafflestudio.snutt.timetablelecturereminder.dto.response

import com.wafflestudio.snutt.timetablelecturereminder.dto.TimetableLectureReminderDto

data class TimetableLectureRemindersWithTimetableIdResponse(
    val timetableId: String?,
    val reminders: List<TimetableLectureReminderDto>,
)
