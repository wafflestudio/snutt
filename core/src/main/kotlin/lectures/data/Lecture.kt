package com.wafflestudio.snu4t.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("lectures")
data class Lecture(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    @Field("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time")
    var periodText: String?,
    @Field("real_class_time")
    var classTimeText: String?,
    @Field("class_time_json")
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
    var semester: Semester,
    var year: Int,
    @Field("course_number")
    var courseNumber: String,
    @Field("course_title")
    var courseTitle: String,
)
