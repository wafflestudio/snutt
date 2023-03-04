package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.timetables.data.TimeTable

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SharedTimeTableDetailResponse(
    val id: String,
    val userId: String,
    val title: String,
    val timetable: TimeTable,
)
