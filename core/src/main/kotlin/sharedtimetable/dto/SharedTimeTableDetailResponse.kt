package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.timetables.data.Timetable

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SharedTimetableDetailResponse(
    val id: String,
    val userId: String,
    val title: String,
    val timetable: Timetable,
)
