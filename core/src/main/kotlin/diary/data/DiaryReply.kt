package com.wafflestudio.snutt.diary.data

import com.wafflestudio.snutt.lectures.data.Lecture
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DiaryReply(
    @Id
    val id: String? = null,
    val question: DiaryQuestion,
    val lecture: Lecture,
)
