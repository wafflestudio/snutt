package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.timetables.dto.TimetableDto

data class SharedTimetableDetailDto(
    val id: String,
    @JsonProperty("user_id")
    val userId: String,
    val title: String,
    val timetable: TimetableDto,
)
