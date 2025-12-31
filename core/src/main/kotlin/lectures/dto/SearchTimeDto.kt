package com.wafflestudio.snutt.lectures.dto

import com.wafflestudio.snutt.common.enums.DayOfWeek

data class SearchTimeDto(
    val day: DayOfWeek,
    val startMinute: Int,
    val endMinute: Int,
)
