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
    val answersShortened: List<String>,
    val forTopics: List<DiaryActivityType>,
    val isActive: Boolean = true,
)
