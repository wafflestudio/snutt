package com.wafflestudio.snutt.diary.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DiaryQuestion(
    @Id
    val id: String? = null,
    val question: String,
    val shortQuestion: String,
    val answers: List<String>,
    val shortAnswers: List<String>,
    val targetActivityTypes: List<DiaryActivityType>,
    var active: Boolean = true,
)
