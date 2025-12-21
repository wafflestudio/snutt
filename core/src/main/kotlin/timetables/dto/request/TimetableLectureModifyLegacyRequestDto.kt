package com.wafflestudio.snutt.timetables.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.theme.data.ColorSet

data class TimetableLectureModifyLegacyRequestDto(
    @param:JsonProperty("course_title")
    val courseTitle: String?,
    @param:JsonProperty("academic_year")
    val academicYear: String?,
    val category: String?,
    val classification: String?,
    val instructor: String?,
    val credit: Long?,
    @param:JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyRequestDto>?,
    val remark: String?,
    val color: ColorSet?,
    val colorIndex: Int?,
    @param:JsonProperty("is_forced")
    val isForced: Boolean = false,
    val categoryPre2025: String?,
)
