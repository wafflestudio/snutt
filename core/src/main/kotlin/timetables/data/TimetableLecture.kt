package com.wafflestudio.snu4t.timetables.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.lectures.data.ClassTime
import com.wafflestudio.snu4t.lectures.data.Lecture
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TimetableLecture(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    @Field("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time")
    @JsonProperty("class_time")
    var periodText: String?,
    @Field("real_class_time")
    @JsonProperty("real_class_time")
    var classTimeText: String?,
    @Field("class_time_json")
    @JsonProperty("class_time_json")
    var classTime: List<ClassTime>,
    @Field("class_time_mask")
    var classTimeMask: List<Int>,
    var classification: String?,
    var credit: Long?,
    var department: String?,
    var instructor: String?,
    @Field("lecture_number")
    var lectureNumber: String?,
    var quota: Int?,
    var remark: String?,
    @Field("course_number")
    var courseNumber: String?,
    @Field("course_title")
    var courseTitle: String,
    var color: ColorSet? = null,
    var colorIndex: Int = 0,
    @JsonProperty("lecture_id")
    @Field("lecture_id", targetType = FieldType.OBJECT_ID)
    @Indexed
    var lectureId: String? = null,
)

fun TimetableLecture(lecture: Lecture): TimetableLecture = TimetableLecture(
    academicYear = lecture.academicYear,
    category = lecture.category,
    periodText = lecture.periodText,
    classTimeText = lecture.classTimeText,
    classTime = lecture.classTime,
    classTimeMask = lecture.classTimeMask,
    classification = lecture.classification,
    credit = lecture.credit,
    department = lecture.department,
    instructor = lecture.instructor,
    lectureNumber = lecture.lectureNumber,
    quota = lecture.quota,
    remark = lecture.remark,
    courseNumber = lecture.courseNumber,
    courseTitle = lecture.courseTitle,
    lectureId = lecture.id,
)
