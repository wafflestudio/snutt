package com.wafflestudio.snutt.timetablelecturereminder.dto

import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureAndReminder

data class TimetableLectureReminderDto(
    val timetableLectureId: String,
    val courseTitle: String,
    val option: TimetableLectureReminderOption,
)

fun TimetableLectureReminderDto(timetableLectureAndReminder: TimetableLectureAndReminder): TimetableLectureReminderDto =
    TimetableLectureReminderDto(
        timetableLectureId = timetableLectureAndReminder.timetableLecture.id,
        courseTitle = timetableLectureAndReminder.timetableLecture.courseTitle,
        option =
            timetableLectureAndReminder.reminder?.let {
                TimetableLectureReminderOption.fromOffsetMinutes(it.offsetMinutes)
            } ?: TimetableLectureReminderOption.NONE,
    )
