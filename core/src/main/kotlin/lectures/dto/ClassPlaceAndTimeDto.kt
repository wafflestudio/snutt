package com.wafflestudio.snutt.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enum.DayOfWeek
import com.wafflestudio.snutt.lecturebuildings.data.LectureBuilding
import com.wafflestudio.snutt.lecturebuildings.data.PlaceInfo
import com.wafflestudio.snutt.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snutt.lectures.utils.endPeriod
import com.wafflestudio.snutt.lectures.utils.minuteToString
import com.wafflestudio.snutt.lectures.utils.startPeriod

data class ClassPlaceAndTimeDto(
    val day: DayOfWeek,
    val place: String?,
    val startMinute: Int,
    val endMinute: Int,
)

fun ClassPlaceAndTimeDto(classPlaceAndTime: ClassPlaceAndTime): ClassPlaceAndTimeDto =
    ClassPlaceAndTimeDto(
        day = classPlaceAndTime.day,
        place = classPlaceAndTime.place,
        startMinute = classPlaceAndTime.startMinute,
        endMinute = classPlaceAndTime.endMinute,
    )

data class ClassPlaceAndTimeLegacyDto(
    val day: DayOfWeek,
    val place: String?,
    val startMinute: Int,
    val endMinute: Int,
    @JsonProperty("start_time")
    val startTime: String,
    @JsonProperty("end_time")
    val endTime: String,
    @JsonProperty("len")
    val periodLength: Double,
    @JsonProperty("start")
    val startPeriod: Double,
    var lectureBuildings: List<LectureBuilding>? = null,
)

fun ClassPlaceAndTimeLegacyDto(classPlaceAndTime: ClassPlaceAndTime): ClassPlaceAndTimeLegacyDto =
    ClassPlaceAndTimeLegacyDto(
        day = classPlaceAndTime.day,
        place = classPlaceAndTime.place,
        startMinute = classPlaceAndTime.startMinute,
        endMinute = classPlaceAndTime.endMinute,
        startTime = minuteToString(classPlaceAndTime.startMinute),
        endTime = minuteToString(classPlaceAndTime.endMinute),
        startPeriod = classPlaceAndTime.startPeriod,
        periodLength = classPlaceAndTime.endPeriod - classPlaceAndTime.startPeriod,
    )

val ClassPlaceAndTimeLegacyDto.placeInfos: List<PlaceInfo>
    get() = place?.let { PlaceInfo.getValuesOf(it) } ?: emptyList()
