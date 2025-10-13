package com.wafflestudio.snutt.diary.dto

data class DiarySubmissionsOfYearSemesterDto(
    val year: Int,
    val semester: Int,
    val submissions: List<DiarySubmissionSummaryDto>,
)
