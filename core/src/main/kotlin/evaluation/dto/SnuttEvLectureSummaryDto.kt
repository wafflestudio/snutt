package com.wafflestudio.snutt.evaluation.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SnuttEvLectureSummaryDto(
    @param:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val snuttId: String,
    val evLectureId: Long,
    val avgRating: Double?,
    val evaluationCount: Long,
)

data class SnuttEvLectureIdDto(
    @param:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val snuttId: String,
    val evLectureId: Long,
)
