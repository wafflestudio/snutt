package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.DayOfWeek
import com.wafflestudio.snu4t.lecturebuildings.data.Campus
import com.wafflestudio.snu4t.lecturebuildings.data.GeoCoordinate
import com.wafflestudio.snu4t.lecturebuildings.data.LectureBuilding
import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snu4t.lectures.utils.endPeriod
import com.wafflestudio.snu4t.lectures.utils.minuteToString
import com.wafflestudio.snu4t.lectures.utils.startPeriod
import org.bson.types.ObjectId

data class ClassPlaceAndTimeDto(
    val day: DayOfWeek,
    val place: String?,
    val startMinute: Int,
    val endMinute: Int,
)

fun ClassPlaceAndTimeDto(classPlaceAndTime: ClassPlaceAndTime): ClassPlaceAndTimeDto = ClassPlaceAndTimeDto(
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
    val lectureBuilding: LectureBuilding?
)

fun ClassPlaceAndTimeLegacyDto(classPlaceAndTime: ClassPlaceAndTime): ClassPlaceAndTimeLegacyDto = ClassPlaceAndTimeLegacyDto(
    day = classPlaceAndTime.day,
    place = classPlaceAndTime.place,
    startMinute = classPlaceAndTime.startMinute,
    endMinute = classPlaceAndTime.endMinute,
    startTime = minuteToString(classPlaceAndTime.startMinute),
    endTime = minuteToString(classPlaceAndTime.endMinute),
    startPeriod = classPlaceAndTime.startPeriod,
    periodLength = classPlaceAndTime.endPeriod - classPlaceAndTime.startPeriod,
    lectureBuilding = MockLectureBuilding()
)

private fun MockLectureBuilding() = LectureBuilding(
    id = ObjectId.get().toHexString(),
    buildingNumber = "500",
    buildingNameKor = "자연과학대학(500)",
    buildingNameEng = "College of Natural Sciences(500)",
    locationInDMS = GeoCoordinate(37.4592190840394, 126.94812006718699),
    locationInDecimal = GeoCoordinate(488525.0, 1099948.0),
    campus = Campus.GWANAK
)
