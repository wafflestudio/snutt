package com.wafflestudio.snu4t.evaluation.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SnuttEvLectureSummaryDto(
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val snuttId: String,
    val evLectureId: Long,
    val avgRating: Double?,
)
