package com.wafflestudio.snutt.bookmark.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLectureModifyRequest(
    val lectureId: String,
)
