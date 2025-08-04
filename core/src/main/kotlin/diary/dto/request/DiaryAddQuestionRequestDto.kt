package com.wafflestudio.snutt.diary.dto.request

data class DiaryAddQuestionRequestDto(
    val question: String,
    val shortQuestion: String,
    val answers: List<String>,
    val shortAnswers: List<String>,
    val targetActivities: List<String>,
    var active: Boolean = true,
)
