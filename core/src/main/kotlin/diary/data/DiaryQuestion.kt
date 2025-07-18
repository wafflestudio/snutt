package com.wafflestudio.snutt.diary.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DiaryQuestion(
    @Id
    val id: String? = null,
    val question: String,
    val shortenedQuestion: String,
    val answers: List<String>,
    val shortenedAnswers: List<String>,
    val targetTopics: List<DiaryActivityType>,
    var active: Boolean = true,
)
