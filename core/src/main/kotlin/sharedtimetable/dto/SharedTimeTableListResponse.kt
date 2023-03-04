package com.wafflestudio.snu4t.sharedtimetable.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.sharedtimetable.data.SharedTimeTable

data class SharedTimeTableListResponse(
    val timetables: List<SharedTimeTable>,
)