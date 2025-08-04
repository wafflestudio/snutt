package com.wafflestudio.snutt.diary.dto.request

data class DiarySubmissionRequestDto(
    val lectureId: String,
    val activities: List<String>,
    val questionIds: List<String>,
    val answerIndexes: List<Int>,
    val comment: String,
)
