package com.wafflestudio.snu4t.lectures.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

data class Lecture(
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
    @Field("course_number")
    var courseNumber: String?,
    @Field("course_title")
    var courseTitle: String?,
)

fun Lecture(lectureWithSemester: LectureWithSemester): Lecture = Lecture(
    id = lectureWithSemester.id,
    academicYear = lectureWithSemester.academicYear,
    category = lectureWithSemester.category,
    classTimeText = lectureWithSemester.classTimeText,
    realClassTimeText = lectureWithSemester.realClassTimeText,
    classTime = lectureWithSemester.classTime,
    classTimeMask = lectureWithSemester.classTimeMask,
    classification = lectureWithSemester.classification,
    credit = lectureWithSemester.credit,
    department = lectureWithSemester.department,
    instructor = lectureWithSemester.instructor,
    lectureNumber = lectureWithSemester.lectureNumber,
    quota = lectureWithSemester.quota,
    remark = lectureWithSemester.remark,
    courseNumber = lectureWithSemester.courseNumber,
    courseTitle = lectureWithSemester.courseTitle,
)
