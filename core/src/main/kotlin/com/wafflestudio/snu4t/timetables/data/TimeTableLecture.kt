package com.wafflestudio.snu4t.timetables.data

import com.wafflestudio.snu4t.lectures.data.ClassTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

data class TimeTableLecture(
        @Id
        var id: String? = null,
        @Field("academic_year")
        var academicYear: String?,
        var category: String?,
        @Field("class_time")
        var classTimeText: String?,
        @Field("real_class_time")
        var realClassTimeText: String?,
        @Field("class_time_json")
        var classTime: List<ClassTime>,
        @Field("class_time_mask")
        var classTimeMask: List<Int>,
        var classification: String?,
        var credit: Int,
        var department: String?,
        var instructor: String?,
        @Field("lecture_number")
        var lectureNumber: String?,
        var quota: Int?,
        var remark: String?,
        var semester: Int?,
        var year: Int?,
        @Field("course_number")
        var courseNumber: String?,
        @Field("course_title")
        var courseTitle: String?,
        var color: ColorSet?,
        var colorIndex: Int?
)