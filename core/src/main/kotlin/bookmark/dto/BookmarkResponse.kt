package com.wafflestudio.snu4t.bookmark.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.wafflestudio.snu4t.bookmark.data.Bookmark
import com.wafflestudio.snu4t.lectures.data.ClassTime
import com.wafflestudio.snu4t.lectures.data.Lecture

class BookmarkResponse(
    val year: Int,
    val semester: Int,
    val lectures: List<BookmarkLectureResponse>,
)

fun BookmarkResponse(bookmark: Bookmark): BookmarkResponse = BookmarkResponse(
    year = bookmark.year,
    semester = bookmark.semester.value,
    lectures = bookmark.lectures.map { BookmarkLectureResponse(it) },
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class BookmarkLectureResponse(
    val id: String? = null,
    val academicYear: String?,
    val category: String?,
    val classTimeText: String?,
    val realClassTimeText: String?,
    val classTime: List<ClassTime>,
    val classTimeMask: List<Int>,
    val classification: String?,
    val credit: Int,
    val department: String?,
    val instructor: String?,
    val lectureNumber: String?,
    val quota: Int?,
    val remark: String?,
    val courseNumber: String?,
    val courseTitle: String?,
)

fun BookmarkLectureResponse(lecture: Lecture): BookmarkLectureResponse = BookmarkLectureResponse(
    id = lecture.id,
    academicYear = lecture.academicYear,
    category = lecture.category,
    classTimeText = lecture.classTimeText,
    realClassTimeText = lecture.realClassTimeText,
    classTime = lecture.classTime,
    classTimeMask = lecture.classTimeMask,
    classification = lecture.classification,
    credit = lecture.credit,
    department = lecture.department,
    instructor = lecture.instructor,
    lectureNumber = lecture.lectureNumber,
    quota = lecture.quota,
    remark = lecture.remark,
    courseNumber = lecture.courseNumber,
    courseTitle = lecture.courseTitle,
)
