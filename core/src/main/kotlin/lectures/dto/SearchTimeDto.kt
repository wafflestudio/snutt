package com.wafflestudio.snutt.lectures.dto

import com.wafflestudio.snutt.common.enum.DayOfWeek

data class SearchTimeDto(
    val day: DayOfWeek,
    val startMinute: Int,
    val endMinute: Int,
)
