package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.utils.ClassTimeUtils

data class LectureDto(
    @JsonProperty("_id")
    val id: String? = null,
    @JsonProperty("academic_year")
    val academicYear: String?,
    val category: String?,
    @JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeDto>,
    val classification: String?,
    val credit: Long,
    val department: String?,
    val instructor: String?,
    @JsonProperty("lecture_number")
    val lectureNumber: String,
    val quota: Int?,
    val freshmanQuota: Int? = null,
    val remark: String?,
    val semester: Semester,
    val year: Int,
    @JsonProperty("course_number")
    val courseNumber: String,
    @JsonProperty("course_title")
    val courseTitle: String,
    val registrationCount: Int,

    // FIXME: 안드로이드 구버전 대응용 필드 1년 후 2024년에 삭제 (2023/06/26)
    @JsonProperty("class_time_mask")
    val classTimeMask: List<Int> = emptyList(),
)

fun LectureDto(lecture: Lecture): LectureDto = LectureDto(
    id = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    classPlaceAndTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeDto(it) },
    classification = lecture.classification,
    credit = lecture.credit,
    department = lecture.department,
    instructor = lecture.instructor,
    lectureNumber = lecture.lectureNumber,
    quota = lecture.quota,
    freshmanQuota = lecture.freshmanQuota,
    remark = lecture.remark,
    semester = lecture.semester,
    year = lecture.year,
    courseNumber = lecture.courseNumber,
    courseTitle = lecture.courseTitle,
    registrationCount = lecture.registrationCount,
    classTimeMask = ClassTimeUtils.classTimeToBitmask(lecture.classPlaceAndTimes),
)
