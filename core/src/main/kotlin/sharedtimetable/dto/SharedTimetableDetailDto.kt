package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.timetables.dto.TimetableDto

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SharedTimetableDetailDto(
    val id: String,
    @JsonProperty("user_id")
    val userId: String,
    val title: String,
    val timetable: TimetableDto,
)
