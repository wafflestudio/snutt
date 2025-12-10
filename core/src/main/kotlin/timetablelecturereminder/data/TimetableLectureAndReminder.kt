package com.wafflestudio.snutt.timetablelecturereminder.data

import com.wafflestudio.snutt.timetables.data.TimetableLecture

data class TimetableLectureAndReminder(
    val timetableLecture: TimetableLecture,
    val reminder: TimetableLectureReminder?,
)
