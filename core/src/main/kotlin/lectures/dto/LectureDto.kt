package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.lectures.data.Lecture

data class LectureDto(
    @JsonProperty("_id")
    val id: String? = null,
    val academicYear: String?,
    val category: String?,
    val periodText: String?,
    val classTimeText: String?,
    val classPlaceAndTimes: List<ClassPlaceAndTimeDto>,
    val classTimeMask: List<Int>,
    val classification: String?,
    val credit: Long,
    val department: String?,
    val instructor: String?,
    val lectureNumber: String,
    val quota: Int?,
    val freshmanQuota: Int? = null,
    val remark: String?,
    val semester: Semester,
    val year: Int,
    val courseNumber: String,
    val courseTitle: String,
    val registrationCount: Int,
)

fun LectureDto(lecture: Lecture): LectureDto = LectureDto(
    id = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    periodText = lecture.periodText,
    classTimeText = lecture.classTimeText,
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
