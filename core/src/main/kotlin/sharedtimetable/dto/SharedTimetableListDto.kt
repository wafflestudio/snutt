package com.wafflestudio.snu4t.sharedtimetable.dto

import java.time.LocalDateTime

data class SharedTimetableListDto(
    val content: List<SharedTimetableBriefDto>
)

data class SharedTimetableBriefDto(
    val id: String,
    val title: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isValid: Boolean,
    val year: Int,
    val semester: Int,
)
