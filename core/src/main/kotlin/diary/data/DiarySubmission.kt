package com.wafflestudio.snutt.diary.data

import com.wafflestudio.snutt.common.enums.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDate
import java.time.LocalDateTime

@CompoundIndex(def = "{ 'userId': 1, 'year': 1, 'semester': 1, 'createdAt': -1 }")
@Document
data class DiarySubmission(
    @Id
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Field(targetType = FieldType.OBJECT_ID)
    val dailyClassTypeIds: List<String>,
    val questionAnswers: List<QuestionAnswer>,
    @Field(targetType = FieldType.OBJECT_ID)
    val lectureId: String,
    val courseTitle: String,
    val comment: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val year: Int,
    val semester: Semester,
    // 일기를 쓴 수업이 열린 날(KST). 수업 날짜 도입 전 기록은 null.
    val classDate: LocalDate? = null,
)

data class QuestionAnswer(
    val questionId: String,
    val answerIndex: Int,
)
