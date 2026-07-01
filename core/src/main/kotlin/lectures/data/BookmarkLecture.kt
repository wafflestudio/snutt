package com.wafflestudio.snutt.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookmarkLecture(
    @Id
    @param:JsonProperty("_id")
    var id: String? = null,
    @Field("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time_json")
    @param:JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTime>,
    var classification: String?,
    var credit: Long,
    var department: String?,
    var instructor: String?,
    @Field("lecture_number")
    var lectureNumber: String,
    var quota: Int?,
    var freshmanQuota: Int?,
    var remark: String?,
    @Field("course_number")
    var courseNumber: String,
    @Field("course_title")
    var courseTitle: String,
    var categoryPre2025: String?,
    @Field("academic_year_en")
    var academicYearEn: String? = null,
    @Field("category_en")
    var categoryEn: String? = null,
    @Field("classification_en")
    var classificationEn: String? = null,
    @Field("department_en")
    var departmentEn: String? = null,
    @Field("instructor_en")
    var instructorEn: String? = null,
    @Field("remark_en")
    var remarkEn: String? = null,
    @Field("course_title_en")
    var courseTitleEn: String? = null,
)

fun BookmarkLecture(lecture: Lecture): BookmarkLecture =
    BookmarkLecture(
        id = lecture.id,
        academicYear = lecture.academicYear,
        category = lecture.category,
        classPlaceAndTimes = lecture.classPlaceAndTimes,
        classification = lecture.classification,
        credit = lecture.credit,
        department = lecture.department,
        instructor = lecture.instructor,
        quota = lecture.quota,
        freshmanQuota = lecture.freshmanQuota,
        remark = lecture.remark,
        lectureNumber = lecture.lectureNumber,
        courseNumber = lecture.courseNumber,
        courseTitle = lecture.courseTitle,
        categoryPre2025 = lecture.categoryPre2025,
        academicYearEn = lecture.academicYearEn,
        categoryEn = lecture.categoryEn,
        classificationEn = lecture.classificationEn,
        departmentEn = lecture.departmentEn,
        instructorEn = lecture.instructorEn,
        remarkEn = lecture.remarkEn,
        courseTitleEn = lecture.courseTitleEn,
    )
