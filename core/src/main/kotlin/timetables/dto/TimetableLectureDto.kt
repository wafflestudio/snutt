package com.wafflestudio.snu4t.timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.lectures.dto.ClassPlaceAndTimeDto
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableLecture

data class TimetableLectureDto(
    @JsonProperty("_id")
    var id: String? = null,
    @JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTimeDto>,
    var classification: String?,
    var credit: Long?,
    var department: String?,
    var instructor: String?,
    var lectureNumber: String?,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    var courseNumber: String?,
    var courseTitle: String,
    var color: ColorSet = ColorSet(),
    @JsonProperty("color_index")
    var colorIndex: Int = 0,
    @JsonProperty("lecture_id")
    var lectureId: String? = null,
)

fun TimetableLectureDto(timetableLecture: TimetableLecture) = TimetableLectureDto(
    id = timetableLecture.id,
    academicYear = timetableLecture.academicYear,
    category = timetableLecture.category,
    classPlaceAndTimes = timetableLecture.classPlaceAndTimes.map { ClassPlaceAndTimeDto(it) },
    classification = timetableLecture.classification,
    credit = timetableLecture.credit,
    department = timetableLecture.department,
    instructor = timetableLecture.instructor,
    lectureNumber = timetableLecture.lectureNumber,
    quota = timetableLecture.quota,
    freshmanQuota = timetableLecture.freshmanQuota,
    remark = timetableLecture.remark,
    courseNumber = timetableLecture.courseNumber,
    courseTitle = timetableLecture.courseTitle,
    color = timetableLecture.color,
    colorIndex = timetableLecture.colorIndex,
    lectureId = timetableLecture.lectureId,
)
