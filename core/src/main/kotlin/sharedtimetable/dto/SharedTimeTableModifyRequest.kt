package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SharedTimeTableCreateRequest(
    val title: String,
    val timetableId: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SharedTimeTableModifyRequest(
    val title: String
)
