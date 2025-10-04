package com.wafflestudio.snutt.timetablelecturereminder.dto

enum class TimetableLectureReminderOption(
    val offsetMinutes: Int?,
) {
    NONE(null),
    TEN_MINUTES_BEFORE(-10),
    ZERO_MINUTES(0),
    TEN_MINUTES_AFTER(10),
    ;

    companion object {
        fun fromOffsetMinutes(offsetMinutes: Int?): TimetableLectureReminderOption =
            when (offsetMinutes) {
                null -> NONE
                -10 -> TEN_MINUTES_BEFORE
                0 -> ZERO_MINUTES
                10 -> TEN_MINUTES_AFTER
                else -> throw IllegalArgumentException("Invalid offsetMinutes: $offsetMinutes")
            }
    }
}
