package com.wafflestudio.snutt.diary.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document
data class DiarySubmission(
    @Id
    val id: String? = null,
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    val activityTypeIds: List<String>,
    val questionIds: List<String>,
    val answerIndexes: List<Int>,
    @Field(targetType = FieldType.OBJECT_ID)
    val lectureId: String,
    val courseTitle: String,
    val comment: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
