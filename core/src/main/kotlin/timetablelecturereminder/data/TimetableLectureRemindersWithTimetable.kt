package com.wafflestudio.snutt.timetablelecturereminder.data

import com.wafflestudio.snutt.timetables.data.Timetable

data class TimetableLectureRemindersWithTimetable(
    val timetable: Timetable,
    val reminders: List<TimetableLectureReminder>,
)
