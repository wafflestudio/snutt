package com.wafflestudio.snu4t.timetables.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.timetables.data.ColorSet

data class TimetableLectureModifyLegacyRequestDto(
    @JsonProperty("_id")
    val id: String,
    @JsonProperty("course_title")
    val courseTitle: String,
    val instructor: String?,
    val credit: Long?,
    @JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyRequestDto>?,
    val remark: String?,
    val color: ColorSet?,
    val colorIndex: Int?,
    val isForced: Boolean = false,
)
