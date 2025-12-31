package com.wafflestudio.snutt.evaluation.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.timetables.data.TimetableLecture

data class EvLectureInfoDto(
    val year: Int,
    val semester: Int,
    val instructor: String?,
    @param:JsonProperty("course_number")
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
