package com.wafflestudio.snutt.bookmark.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BookmarkLectureModifyRequest(
    @param:JsonProperty("lecture_id")
    val lectureId: String,
)
