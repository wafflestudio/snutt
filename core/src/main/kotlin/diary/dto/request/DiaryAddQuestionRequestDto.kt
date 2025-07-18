package com.wafflestudio.snutt.diary.dto.request

data class DiaryAddQuestionRequestDto(
    val question: String,
    val shortenedQuestion: String,
    val answers: List<String>,
    val shortenedAnswers: List<String>,
    val targetTopics: List<String>,
    var active: Boolean = true,
)
