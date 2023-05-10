package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.common.enum.DayOfWeek

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ClassPlaceAndTime(
    val day: DayOfWeek,
    val place: String,
    val startMinute: Int,
    val endMinute: Int,
)
