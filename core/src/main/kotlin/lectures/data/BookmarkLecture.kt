package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLecture(
    @Id
    var id: String? = null,
    @Field("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time")
    @JsonProperty("class_time")
    val classTimeText: String?,
    @Field("real_class_time")
    @JsonProperty("real_class_time")
    var realClassTimeText: String?,
    @Field("class_time_json")
    @JsonProperty("class_time_json")
    var classTime: List<ClassTime>,
    @Field("class_time_mask")
    var classTimeMask: List<Int>,
    var classification: String?,
    var credit: Int,
    var department: String?,
    var instructor: String?,
    @Field("lecture_number")
    var lectureNumber: String,
    var quota: Int?,
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
    classTimeText = lecture.classTimeText,
    realClassTimeText = lecture.realClassTimeText,
    classTime = lecture.classTime,
    classTimeMask = lecture.classTimeMask,
    classification = lecture.classification,
    credit = lecture.credit,
    department = lecture.department,
    instructor = lecture.instructor,
    quota = lecture.quota,
    remark = lecture.remark,
    lectureNumber = lecture.lectureNumber,
    courseNumber = lecture.courseNumber,
    courseTitle = lecture.courseTitle,
)
