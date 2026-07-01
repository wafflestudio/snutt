package com.wafflestudio.snutt.timetables.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.lectures.data.ClassPlaceAndTime
import com.wafflestudio.snutt.lectures.data.Lecture
import com.wafflestudio.snutt.theme.data.ColorSet
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TimetableLecture(
    @Id
    @param:JsonProperty("_id")
    var id: String = ObjectId.get().toHexString(),
    @Field("academic_year")
    @param:JsonProperty("academic_year")
    var academicYear: String?,
    var category: String?,
    @Field("class_time_json")
    @param:JsonProperty("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTime>,
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
    var color: ColorSet? = null,
    @param:JsonProperty("color_index")
    var colorIndex: Int = 0,
    @param:JsonProperty("lecture_id")
    @Field("lecture_id", targetType = FieldType.OBJECT_ID)
    @Indexed
    var lectureId: String? = null,
    var categoryPre2025: String?,
    @Field("academic_year_en")
    @param:JsonProperty("academic_year_en")
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

fun TimetableLecture(
    lecture: Lecture,
    colorIndex: Int,
    color: ColorSet?,
) = TimetableLecture(
    lectureId = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    classPlaceAndTimes = lecture.classPlaceAndTimes,
    classification = lecture.classification,
    credit = lecture.credit,
    department = lecture.department,
    instructor = lecture.instructor,
    lectureNumber = lecture.lectureNumber,
    quota = lecture.quota,
    freshmanQuota = lecture.freshmanQuota,
    remark = lecture.remark,
    courseNumber = lecture.courseNumber,
    courseTitle = lecture.courseTitle,
    colorIndex = colorIndex,
    color = color,
    categoryPre2025 = lecture.categoryPre2025,
    academicYearEn = lecture.academicYearEn,
    categoryEn = lecture.categoryEn,
    classificationEn = lecture.classificationEn,
    departmentEn = lecture.departmentEn,
    instructorEn = lecture.instructorEn,
    remarkEn = lecture.remarkEn,
    courseTitleEn = lecture.courseTitleEn,
)
