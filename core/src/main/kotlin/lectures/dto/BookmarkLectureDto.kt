package com.wafflestudio.snutt.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.lectures.data.BookmarkLecture

data class BookmarkLectureDto(
    @JsonProperty("_id")
    var id: String? = null,
    @JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @JsonProperty("class_time_json")
    var classTimes: List<ClassPlaceAndTimeLegacyDto>,
    var classification: String?,
    var credit: Long,
    var department: String?,
    var instructor: String?,
    @JsonProperty("lecture_number")
    var lectureNumber: String,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    @JsonProperty("course_number")
    var courseNumber: String,
    @JsonProperty("course_title")
    var courseTitle: String,
    val snuttEvLecture: SnuttEvLectureSummaryDto? = null,
)

fun BookmarkLectureDto(
    lecture: BookmarkLecture,
    snuttEvLecture: SnuttEvLectureSummaryDto? = null,
): BookmarkLectureDto =
    BookmarkLectureDto(
        id = lecture.id,
        academicYear = lecture.academicYear,
        category = lecture.category,
        classTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeLegacyDto(it) },
        classification = lecture.classification,
        credit = lecture.credit,
        department = lecture.department,
        instructor = lecture.instructor,
        quota = lecture.quota,
        freshmanQuota = lecture.freshmanQuota,
        remark = lecture.remark,
        lectureNumber = lecture.lectureNumber,
        courseNumber = lecture.courseNumber,
        courseTitle = lecture.courseTitle,
        snuttEvLecture = snuttEvLecture,
    )
