package com.wafflestudio.snutt.timetables.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.client.Language
import com.wafflestudio.snutt.common.client.select
import com.wafflestudio.snutt.common.enums.LectureCategoryPre2025
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
    language: Language = Language.KO,
) = TimetableLectureDto(
    id = timetableLecture.id,
    academicYear = language.select(timetableLecture.academicYear, timetableLecture.academicYearEn),
    category = language.select(timetableLecture.category, timetableLecture.categoryEn),
    classPlaceAndTimes = timetableLecture.classPlaceAndTimes.map { ClassPlaceAndTimeDto(it) },
    classification = language.select(timetableLecture.classification, timetableLecture.classificationEn),
    credit = timetableLecture.credit,
    department = language.select(timetableLecture.department, timetableLecture.departmentEn),
    instructor = language.select(timetableLecture.instructor, timetableLecture.instructorEn),
    lectureNumber = timetableLecture.lectureNumber,
    quota = timetableLecture.quota,
    freshmanQuota = timetableLecture.freshmanQuota,
    remark = language.select(timetableLecture.remark, timetableLecture.remarkEn),
    courseNumber = timetableLecture.courseNumber,
    courseTitle = language.select(timetableLecture.courseTitle, timetableLecture.courseTitleEn),
    color = timetableLecture.color,
    colorIndex = timetableLecture.colorIndex,
    lectureId = timetableLecture.lectureId,
    snuttEvLecture = snuttEvLecture,
    categoryPre2025 = timetableLecture.categoryPre2025?.let { LectureCategoryPre2025.localize(it, language) },
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
    language: Language = Language.KO,
) = TimetableLectureLegacyDto(
    id = timetableLecture.id,
    academicYear = language.select(timetableLecture.academicYear, timetableLecture.academicYearEn),
    category = language.select(timetableLecture.category, timetableLecture.categoryEn),
    classPlaceAndTimes = timetableLecture.classPlaceAndTimes.map { ClassPlaceAndTimeLegacyDto(it) },
    classification = language.select(timetableLecture.classification, timetableLecture.classificationEn),
    credit = timetableLecture.credit,
    department = language.select(timetableLecture.department, timetableLecture.departmentEn),
    instructor = language.select(timetableLecture.instructor, timetableLecture.instructorEn),
    lectureNumber = timetableLecture.lectureNumber,
    quota = timetableLecture.quota,
    freshmanQuota = timetableLecture.freshmanQuota,
    remark = language.select(timetableLecture.remark, timetableLecture.remarkEn),
    courseNumber = timetableLecture.courseNumber,
    courseTitle = language.select(timetableLecture.courseTitle, timetableLecture.courseTitleEn),
    color = timetableLecture.color,
    colorIndex = timetableLecture.colorIndex,
    lectureId = timetableLecture.lectureId,
    snuttEvLecture = snuttEvLecture,
    categoryPre2025 = timetableLecture.categoryPre2025?.let { LectureCategoryPre2025.localize(it, language) },
)
