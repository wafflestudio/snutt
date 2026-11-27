package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiaryTargetCandidate
import java.time.LocalDate

data class DiaryTargetLectureCandidateDto(
    val lectureId: String,
    val courseTitle: String,
    val heldToday: Boolean,
    val writable: Boolean,
    val lastClassDate: LocalDate?,
)

fun DiaryTargetLectureCandidateDto(candidate: DiaryTargetCandidate): DiaryTargetLectureCandidateDto =
    DiaryTargetLectureCandidateDto(
        lectureId = candidate.lecture.lectureId!!,
        courseTitle = candidate.lecture.courseTitle,
        heldToday = candidate.heldToday,
        writable = candidate.writable,
        lastClassDate = candidate.lastClassDate,
    )
