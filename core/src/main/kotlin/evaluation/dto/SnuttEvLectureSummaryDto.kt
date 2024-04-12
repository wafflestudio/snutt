package com.wafflestudio.snu4t.evaluation.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SnuttEvLectureSummaryDto(
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "snutt_id")
    val snuttId: String,
    @JsonProperty(value = "ev_lecture_id")
    val evLectureId: Long,
    @JsonProperty(value = "avg_rating")
    val avgRating: Double?,
)
