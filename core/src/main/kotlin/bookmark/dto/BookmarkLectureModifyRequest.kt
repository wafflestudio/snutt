package com.wafflestudio.snutt.bookmark.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLectureModifyRequest(
    val lectureId: String,
)
