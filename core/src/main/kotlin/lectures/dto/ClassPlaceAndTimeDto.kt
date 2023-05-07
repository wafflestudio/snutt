package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snu4t.lectures.utils.endPeriod
import com.wafflestudio.snu4t.lectures.utils.startPeriod

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ClassPlaceAndTimeDto(
    val day: DayOfWeek,
    val place: String?,
    val startMinute: Int,
    val endMinute: Int,
    val startTime: String,
    val endTime: String,
    @JsonProperty("len")
    val periodLength: Double,
    @JsonProperty("start")
    val startPeriod: Double,
)

fun ClassPlaceAndTimeDto(classPlaceAndTime: ClassPlaceAndTime): ClassPlaceAndTimeDto = ClassPlaceAndTimeDto(
    day = classPlaceAndTime.day,
    place = classPlaceAndTime.place,
    startMinute = classPlaceAndTime.startMinute,
    endMinute = classPlaceAndTime.endMinute,
    startTime = "${classPlaceAndTime.startMinute / 60}:${classPlaceAndTime.startMinute % 60}",
    endTime = "${classPlaceAndTime.endMinute / 60}:${classPlaceAndTime.endMinute % 60}",
    startPeriod = classPlaceAndTime.startPeriod,
    periodLength = classPlaceAndTime.endPeriod - classPlaceAndTime.startPeriod,
)
