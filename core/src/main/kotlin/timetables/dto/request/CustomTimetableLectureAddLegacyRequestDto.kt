package com.wafflestudio.snu4t.timetables.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableLecture

data class CustomTimetableLectureAddLegacyRequestDto(
    @JsonProperty("course_title")
    val courseTitle: String,
    val instructor: String?,
    val credit: Long?,
    @JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyRequestDto> = listOf(),
    val remark: String?,
    val color: ColorSet?,
    val colorIndex: Int?,
    @JsonProperty("is_forced")
    val isForced: Boolean = false,
) {
    fun toTimetableLecture(): TimetableLecture {
        val classPlaceAndTimes = this.classPlaceAndTimes.map { it.toClassPlaceAndTime() }
        return TimetableLecture(
            courseTitle = courseTitle,
            instructor = instructor,
            credit = credit,
            color = color ?: ColorSet(),
            colorIndex = colorIndex ?: 0,
            remark = remark,
            classPlaceAndTimes = classPlaceAndTimes,
            classification = null,
            category = null,
            department = null,
            courseNumber = null,
            lectureNumber = null,
            quota = null,
            freshmanQuota = null,
            academicYear = null,
        )
    }
}
