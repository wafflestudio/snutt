package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryTargetCandidate
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import java.time.LocalDate

data class DiaryTargetLectureDto(
    val lectureId: String,
    val courseTitle: String,
    val lastClassDate: LocalDate? = null,
)

fun DiaryTargetLectureDto(timetableLecture: TimetableLecture): DiaryTargetLectureDto =
    DiaryTargetLectureDto(
        lectureId = timetableLecture.lectureId!!,
        courseTitle = timetableLecture.courseTitle,
    )

fun DiaryTargetLectureDto(candidate: DiaryTargetCandidate): DiaryTargetLectureDto =
    DiaryTargetLectureDto(
        lectureId = candidate.lecture.lectureId!!,
        courseTitle = candidate.lecture.courseTitle,
        lastClassDate = candidate.lastClassDate,
    )
