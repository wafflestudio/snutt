package com.wafflestudio.snutt.lectures.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.common.enums.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("lectures")
@CompoundIndex(def = "{ 'year': 1, 'semester': 1 }")
@CompoundIndex(def = "{ 'course_number': 1, 'lecture_number': 1 }")
data class Lecture(
    @Id
    @param:JsonProperty("_id")
    var id: String? = null,
    @Field("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTime>,
    var classification: String?,
    var credit: Long,
    var department: String?,
    var instructor: String?,
    @Field("lecture_number")
    var lectureNumber: String,
    var quota: Int,
    var freshmanQuota: Int? = null,
    var remark: String?,
    var semester: Semester,
    var year: Int,
    @Field("course_number")
    var courseNumber: String,
    @Field("course_title")
    var courseTitle: String,
    var registrationCount: Int = 0,
    var wasFull: Boolean = false,
    var evInfo: EvInfo? = null,
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
) {
    infix fun equalsMetadata(other: Lecture): Boolean =
        this === other ||
            (
                academicYear == other.academicYear &&
                    category == other.category &&
                    classPlaceAndTimes == other.classPlaceAndTimes &&
                    classification == other.classification &&
                    credit == other.credit &&
                    department == other.department &&
                    instructor == other.instructor &&
                    lectureNumber == other.lectureNumber &&
                    quota == other.quota &&
                    freshmanQuota == other.freshmanQuota &&
                    remark == other.remark &&
                    semester == other.semester &&
                    year == other.year &&
                    courseNumber == other.courseNumber &&
                    courseTitle == other.courseTitle &&
                    categoryPre2025 == other.categoryPre2025 &&
                    academicYearEn == other.academicYearEn &&
                    categoryEn == other.categoryEn &&
                    classificationEn == other.classificationEn &&
                    departmentEn == other.departmentEn &&
                    instructorEn == other.instructorEn &&
                    remarkEn == other.remarkEn &&
                    courseTitleEn == other.courseTitleEn
            )
}
