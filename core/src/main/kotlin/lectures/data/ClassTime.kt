package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.common.enum.DayOfWeek
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ClassTime(
    @Id
    var id: String,
    var day: DayOfWeek,
    var place: String,
    @Field("start_time")
    var startTime: String,
    @Field("end_time")
    var endTime: String,
    @Field("len")
    @JsonProperty("len")
    var length: Double,
    @Field("start")
    @JsonProperty("start")
    var startPeriod: Double,
)
