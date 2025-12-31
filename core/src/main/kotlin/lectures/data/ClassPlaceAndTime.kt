package com.wafflestudio.snutt.lectures.data

import com.wafflestudio.snutt.common.enums.DayOfWeek

data class ClassPlaceAndTime(
    val day: DayOfWeek,
    val place: String,
    val startMinute: Int,
    val endMinute: Int,
)
