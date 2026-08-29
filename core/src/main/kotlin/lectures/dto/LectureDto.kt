package com.wafflestudio.snutt.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.client.Language
import com.wafflestudio.snutt.common.client.select
import com.wafflestudio.snutt.common.enums.LectureCategoryPre2025
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.lectures.data.Lecture

data class LectureDto(
    @param:JsonProperty("_id")
    val id: String? = null,
    @param:JsonProperty("academic_year")
    val academicYear: String?,
    val category: String?,
    @param:JsonProperty("class_time_json")
    val classPlaceAndTimes: List<ClassPlaceAndTimeLegacyDto>,
    val classification: String?,
    val credit: Long,
    val department: String?,
    val instructor: String?,
    @param:JsonProperty("lecture_number")
    val lectureNumber: String,
    val quota: Int?,
    val freshmanQuota: Int? = null,
    val remark: String?,
    val semester: Semester,
    val year: Int,
    @param:JsonProperty("course_number")
    val courseNumber: String,
    @param:JsonProperty("course_title")
    val courseTitle: String,
    val registrationCount: Int,
    val wasFull: Boolean,
    val snuttEvLecture: SnuttEvLectureSummaryDto? = null,
    val categoryPre2025: String?,
)

fun LectureDto(
    lecture: Lecture,
    snuttevLecture: SnuttEvLectureSummaryDto? = null,
    language: Language = Language.KO,
): LectureDto =
    LectureDto(
        id = lecture.id,
        academicYear = language.select(lecture.academicYear, lecture.academicYearEn),
        category = language.select(lecture.category, lecture.categoryEn),
        classPlaceAndTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeLegacyDto(it) },
        classification = language.select(lecture.classification, lecture.classificationEn),
        credit = lecture.credit,
        department = language.select(lecture.department, lecture.departmentEn),
        instructor = language.select(lecture.instructor, lecture.instructorEn),
        lectureNumber = lecture.lectureNumber,
        quota = lecture.quota,
        freshmanQuota = lecture.freshmanQuota,
        remark = language.select(lecture.remark, lecture.remarkEn),
        semester = lecture.semester,
        year = lecture.year,
        courseNumber = lecture.courseNumber,
        courseTitle = language.select(lecture.courseTitle, lecture.courseTitleEn),
        registrationCount = lecture.registrationCount,
        wasFull = lecture.wasFull,
        snuttEvLecture = snuttevLecture,
        categoryPre2025 = lecture.categoryPre2025?.let { LectureCategoryPre2025.localize(it, language) },
    )
