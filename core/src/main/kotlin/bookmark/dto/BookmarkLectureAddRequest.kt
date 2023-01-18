package com.wafflestudio.snu4t.bookmark.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLectureAddRequest(
    val lectureId: String,
)
