package com.wafflestudio.snutt.lectures.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.client.Language
import com.wafflestudio.snutt.common.client.select
import com.wafflestudio.snutt.evaluation.dto.SnuttEvLectureSummaryDto
import com.wafflestudio.snutt.lectures.data.BookmarkLecture

data class BookmarkLectureDto(
    @param:JsonProperty("_id")
    var id: String? = null,
    @param:JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @param:JsonProperty("class_time_json")
    var classTimes: List<ClassPlaceAndTimeLegacyDto>,
    var classification: String?,
    var credit: Long,
    var department: String?,
    var instructor: String?,
    @param:JsonProperty("lecture_number")
    var lectureNumber: String,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    @param:JsonProperty("course_number")
    var courseNumber: String,
    @param:JsonProperty("course_title")
    var courseTitle: String,
    val snuttEvLecture: SnuttEvLectureSummaryDto? = null,
)

fun BookmarkLectureDto(
    lecture: BookmarkLecture,
    snuttEvLecture: SnuttEvLectureSummaryDto? = null,
    language: Language = Language.KO,
): BookmarkLectureDto =
    BookmarkLectureDto(
        id = lecture.id,
        academicYear = language.select(lecture.academicYear, lecture.academicYearEn),
        category = language.select(lecture.category, lecture.categoryEn),
        classTimes = lecture.classPlaceAndTimes.map { ClassPlaceAndTimeLegacyDto(it) },
        classification = language.select(lecture.classification, lecture.classificationEn),
        credit = lecture.credit,
        department = language.select(lecture.department, lecture.departmentEn),
        instructor = language.select(lecture.instructor, lecture.instructorEn),
        quota = lecture.quota,
        freshmanQuota = lecture.freshmanQuota,
        remark = language.select(lecture.remark, lecture.remarkEn),
        lectureNumber = lecture.lectureNumber,
        courseNumber = lecture.courseNumber,
        courseTitle = language.select(lecture.courseTitle, lecture.courseTitleEn),
        snuttEvLecture = snuttEvLecture,
    )
