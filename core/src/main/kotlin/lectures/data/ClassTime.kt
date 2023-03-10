package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.common.enum.DayOfWeek
import org.springframework.data.mongodb.core.mapping.Field

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ClassTime(
    val day: DayOfWeek,
    val place: String,
    @Field("start_time")
    val startTime: String,
    @Field("end_time")
    val endTime: String,
    @Field("len")
    @JsonProperty("len")
    val periodLength: Double,
    @Field("start")
    @JsonProperty("start")
    val startPeriod: Double,
)
