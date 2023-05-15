package com.wafflestudio.snu4t.timetables.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.lectures.data.ClassPlaceAndTime
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
    @JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time_json")
    @JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTime>,
    @Field("class_time_mask")
    var classTimeMask: List<Int>,
    var classification: String?,
    var credit: Long?,
    var department: String?,
    var instructor: String?,
    @Field("lecture_number")
    var lectureNumber: String?,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    @Field("course_number")
    var courseNumber: String?,
    @Field("course_title")
    var courseTitle: String,
    var color: ColorSet = ColorSet(),
    @JsonProperty("color_index")
    var colorIndex: Int = 0,
    @JsonProperty("lecture_id")
    @Field("lecture_id", targetType = FieldType.OBJECT_ID)
    @Indexed
    var lectureId: String? = null,
)
