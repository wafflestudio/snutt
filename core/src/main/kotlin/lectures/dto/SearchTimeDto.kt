package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.common.enum.DayOfWeek

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SearchTimeDto(
    val day: DayOfWeek,
    val startMinute: Int,
    val endMinute: Int,
)
