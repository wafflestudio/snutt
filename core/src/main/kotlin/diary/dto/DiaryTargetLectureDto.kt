package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.timetables.data.TimetableLecture

data class DiaryTargetLectureDto(
    val lectureId: String,
    val courseTitle: String,
)

fun DiaryTargetLectureDto(timetableLecture: TimetableLecture): DiaryTargetLectureDto =
    DiaryTargetLectureDto(
        lectureId = timetableLecture.lectureId!!,
        courseTitle = timetableLecture.courseTitle,
    )
