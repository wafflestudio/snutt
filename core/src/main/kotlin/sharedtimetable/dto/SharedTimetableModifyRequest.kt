package com.wafflestudio.snu4t.sharedtimetable.dto

data class SharedTimetableCreateRequest(
    val title: String,
    val timetableId: String
)

data class SharedTimetableModifyRequest(
    val title: String
)
