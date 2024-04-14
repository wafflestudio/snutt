package com.wafflestudio.snu4t.timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snu4t.lectures.dto.ClassPlaceAndTimeDto
import com.wafflestudio.snu4t.lectures.dto.ClassPlaceAndTimeLegacyDto
import com.wafflestudio.snu4t.lectures.utils.ClassTimeUtils
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableLecture

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
    val snuttEvLecture: SnuttEvLectureSummaryDto? = null,
)

fun TimetableLectureDto(timetableLecture: TimetableLecture, snuttEvLecture: SnuttEvLectureSummaryDto? = null) = TimetableLectureDto(
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
)

data class TimetableLectureLegacyDto(
    @JsonProperty("_id")
    var id: String? = null,
    @JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTimeLegacyDto>,
    var classification: String?,
    var credit: Long?,
    var department: String?,
    var instructor: String?,
    @JsonProperty("lecture_number")
    var lectureNumber: String?,
    var quota: Int?,
    @JsonProperty("freshman_quota")
    var freshmanQuota: Int?,
    var remark: String?,
    @JsonProperty("course_number")
    var courseNumber: String?,
    @JsonProperty("course_title")
    var courseTitle: String,
    var color: ColorSet?,
    var colorIndex: Int = 0,
    @JsonProperty("lecture_id")
    var lectureId: String? = null,
    val snuttEvLecture: SnuttEvLectureSummaryDto? = null,

    // FIXME: 안드로이드 구버전 대응용 필드 1년 후 2024년에 삭제 (2023/06/26)
    @JsonProperty("class_time_mask")
    val classTimeMask: List<Int> = emptyList(),
)

fun TimetableLectureLegacyDto(timetableLecture: TimetableLecture, snuttEvLecture: SnuttEvLectureSummaryDto? = null) =
    TimetableLectureLegacyDto(
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
        classTimeMask = ClassTimeUtils.classTimeToBitmask(timetableLecture.classPlaceAndTimes),
        snuttEvLecture = snuttEvLecture,
    )
