package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.lectures.utils.toClassTimeText
import com.wafflestudio.snu4t.lectures.utils.toPeriodText

data class LectureDto(
    @JsonProperty("_id")
    val id: String? = null,
    @JsonProperty("academic_year")
    val academicYear: String?,
    val category: String?,
    @JsonProperty("class_time")
    val periodText: String?,
    @JsonProperty("real_class_time")
    val classTimeText: String?,
    @JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeDto>,
    @JsonProperty("class_time_mask")
    val classTimeMask: List<Int>,
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
)

fun LectureDto(lecture: Lecture): LectureDto = LectureDto(
    id = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    periodText = lecture.classPlaceAndTimes.toPeriodText(),
    classTimeText = lecture.classPlaceAndTimes.toClassTimeText(),
    classPlaceAndTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeDto(it) },
    classTimeMask = lecture.classTimeMask,
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
    registrationCount = lecture.registrationCount
)
