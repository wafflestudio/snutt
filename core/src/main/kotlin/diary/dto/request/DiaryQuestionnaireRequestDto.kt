package com.wafflestudio.snutt.diary.dto.request

data class DiaryQuestionnaireRequestDto(
    val lectureId: String,
    val activityTypes: List<String>,
)
