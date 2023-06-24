package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLecture(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    @Field("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time_json")
    @JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTime>,
    var classification: String?,
    var credit: Long,
    var department: String?,
    var instructor: String?,
    @Field("lecture_number")
    var lectureNumber: String,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    @Field("course_number")
    var courseNumber: String,
    @Field("course_title")
    var courseTitle: String,
)

fun BookmarkLecture(lecture: Lecture): BookmarkLecture = BookmarkLecture(
    id = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    classPlaceAndTimes = lecture.classPlaceAndTimes,
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
