package com.wafflestudio.snutt.timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureIdDto
import com.wafflestudio.snutt.lectures.dto.ClassPlaceAndTimeDto
import com.wafflestudio.snutt.lectures.dto.ClassPlaceAndTimeLegacyDto
import com.wafflestudio.snutt.theme.data.ColorSet
import com.wafflestudio.snutt.timetables.data.TimetableLecture

data class TimetableLectureDto(
    var id: String? = null,
    var academicYear: String?,
    var category: String?,
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
    var color: ColorSet?,
    var colorIndex: Int = 0,
    var lectureId: String? = null,
    val snuttEvLecture: SnuttEvLectureIdDto? = null,
    val categoryPre2025: String?,
)

fun TimetableLectureDto(
    timetableLecture: TimetableLecture,
    snuttEvLecture: SnuttEvLectureIdDto? = null,
) = TimetableLectureDto(
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
    snuttEvLecture = snuttEvLecture,
    categoryPre2025 = timetableLecture.categoryPre2025,
)

data class TimetableLectureLegacyDto(
    @param:JsonProperty("_id")
    var id: String? = null,
    @param:JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @param:JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTimeLegacyDto>,
    var classification: String?,
    var credit: Long?,
    var department: String?,
    var instructor: String?,
    @param:JsonProperty("lecture_number")
    var lectureNumber: String?,
    var quota: Int?,
    @param:JsonProperty("freshman_quota")
    var freshmanQuota: Int?,
    var remark: String?,
    @param:JsonProperty("course_number")
    var courseNumber: String?,
    @param:JsonProperty("course_title")
    var courseTitle: String,
    var color: ColorSet?,
    var colorIndex: Int = 0,
    @param:JsonProperty("lecture_id")
    var lectureId: String? = null,
    val snuttEvLecture: SnuttEvLectureIdDto? = null,
    val categoryPre2025: String?,
)

fun TimetableLectureLegacyDto(
    timetableLecture: TimetableLecture,
    snuttEvLecture: SnuttEvLectureIdDto? = null,
) = TimetableLectureLegacyDto(
    id = timetableLecture.id,
    academicYear = timetableLecture.academicYear,
    category = timetableLecture.category,
    classPlaceAndTimes = timetableLecture.classPlaceAndTimes.map { ClassPlaceAndTimeLegacyDto(it) },
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
    snuttEvLecture = snuttEvLecture,
    categoryPre2025 = timetableLecture.categoryPre2025,
)
