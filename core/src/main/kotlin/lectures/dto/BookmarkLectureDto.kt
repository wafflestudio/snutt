package com.wafflestudio.snu4t.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.lectures.data.BookmarkLecture

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLectureDto(
    @JsonProperty("_id")
    var id: String? = null,
    var academicYear: String?,
    var category: String?,
    @JsonProperty("class_time_json")
    var classTimes: List<ClassPlaceAndTimeDto>,
    var classification: String?,
    var credit: Long,
    var department: String?,
    var instructor: String?,
    var lectureNumber: String,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    var courseNumber: String,
    var courseTitle: String,

    // FIXME: 안드로이드 구버전 대응용 필드 1년 후 2024년에 삭제 (2023/06/26)
    @JsonProperty("class_time_mask")
    val classTimeMask: List<Int> = emptyList(),
)

fun BookmarkLectureDto(lecture: BookmarkLecture): BookmarkLectureDto = BookmarkLectureDto(
    id = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    classTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeDto(it) },
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
)
