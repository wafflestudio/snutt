package com.wafflestudio.snu4t.evaluation.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.timetables.data.TimetableLecture

data class EvLectureInfoDto(
    val year: Int,
    val semester: Int,
    val instructor: String?,
    @JsonProperty("course_number")
    val courseNumber: String?,
)

fun EvLectureInfoDto(
    timetableLecture: TimetableLecture,
    year: Int,
    semester: Semester,
): EvLectureInfoDto =
    EvLectureInfoDto(
        year = year,
        semester = semester.value,
        instructor = timetableLecture.instructor,
        courseNumber = timetableLecture.courseNumber,
    )
