package com.wafflestudio.snutt.timetablelecturereminder.data

import com.wafflestudio.snutt.timetables.data.Timetable

data class TimetableAndReminder(
    val timetable: Timetable,
    val reminder: TimetableLectureReminder,
)
