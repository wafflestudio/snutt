package com.wafflestudio.snutt.diary.dto

import java.time.LocalDateTime

data class DiarySubmissionSummaryDto(
    val date: LocalDateTime,
    val lectureTitle: String,
    val shortQuestionReplies: List<DiaryShortQuestionReply>,
    val comment: String,
)

data class DiaryShortQuestionReply(
    val question: String,
    val answer: String,
)
