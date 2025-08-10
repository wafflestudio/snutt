package com.wafflestudio.snutt.diary.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document
data class DiaryQuestion(
    @Id
    val id: String? = null,
    val question: String,
    val shortQuestion: String,
    val answers: List<String>,
    val shortAnswers: List<String>,
    @Field(targetType = FieldType.OBJECT_ID)
    val targetActivityIds: List<String>,
    var active: Boolean = true,
)
