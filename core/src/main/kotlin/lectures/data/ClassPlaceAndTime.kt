package com.wafflestudio.snutt.lectures.data

import com.wafflestudio.snutt.common.enum.DayOfWeek

data class ClassPlaceAndTime(
    val day: DayOfWeek,
    val place: String,
    val startMinute: Int,
    val endMinute: Int,
)
