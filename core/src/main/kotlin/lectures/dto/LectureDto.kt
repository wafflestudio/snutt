package com.wafflestudio.snutt.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.lectures.data.Lecture

data class LectureDto(
    @JsonProperty("_id")
    val id: String? = null,
    @JsonProperty("academic_year")
    val academicYear: String?,
    val category: String?,
    @JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyDto>,
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
    val wasFull: Boolean,
    val snuttEvLecture: SnuttEvLectureSummaryDto? = null,
    val categoryPre2025: String?,
)

fun LectureDto(
    lecture: Lecture,
    snuttevLecture: SnuttEvLectureSummaryDto? = null,
): LectureDto =
    LectureDto(
        id = lecture.id,
        academicYear = lecture.academicYear,
        category = lecture.category,
        classPlaceAndTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeLegacyDto(it) },
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
        wasFull = lecture.wasFull,
        snuttEvLecture = snuttevLecture,
        categoryPre2025 = lecture.categoryPre2025,
    )
