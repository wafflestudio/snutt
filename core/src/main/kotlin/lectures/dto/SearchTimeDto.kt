package com.wafflestudio.snu4t.lectures.dto

import com.wafflestudio.snu4t.common.enum.DayOfWeek

data class SearchTimeDto(
    val day: DayOfWeek,
    val startMinute: Int,
    val endMinute: Int,
)
