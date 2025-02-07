package com.wafflestudio.snutt.timetables.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.theme.data.ColorSet

data class TimetableLectureModifyLegacyRequestDto(
    @JsonProperty("course_title")
    val courseTitle: String?,
    @JsonProperty("academic_year")
    val academicYear: String?,
    val category: String?,
    val classification: String?,
    val instructor: String?,
    val credit: Long?,
    @JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyRequestDto>?,
    val remark: String?,
    val color: ColorSet?,
    val colorIndex: Int?,
    @JsonProperty("is_forced")
    val isForced: Boolean = false,
    val categoryPre2025: String?,
)
