package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SharedTimeTableRequest(
    val title: String,
    @JsonProperty("timetable_id")
    val timeTableId: String
)
