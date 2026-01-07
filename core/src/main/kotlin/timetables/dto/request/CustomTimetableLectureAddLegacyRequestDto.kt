package com.wafflestudio.snutt.timetables.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.theme.data.ColorSet
import com.wafflestudio.snutt.timetables.data.TimetableLecture

data class CustomTimetableLectureAddLegacyRequestDto(
    @param:JsonProperty("course_title")
    val courseTitle: String,
    val instructor: String?,
    val credit: Long?,
    @param:JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyRequestDto> = listOf(),
    val remark: String?,
    val color: ColorSet?,
    val colorIndex: Int?,
    @param:JsonProperty("is_forced")
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
            categoryPre2025 = null,
        )
    }
}
