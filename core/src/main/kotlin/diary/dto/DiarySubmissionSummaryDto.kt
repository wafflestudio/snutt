package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.diary.data.DiarySubmission
import java.time.LocalDateTime

data class DiarySubmissionSummaryDto(
    val id: String,
    val date: LocalDateTime,
    val lectureTitle: String,
    val shortQuestionReplies: List<DiaryShortQuestionReply>,
    val comment: String,
)

data class DiaryShortQuestionReply(
    val question: String,
    val answer: String,
)

fun DiarySubmissionSummaryDto(
    submission: DiarySubmission,
    shortQuestionReplies: List<DiaryShortQuestionReply>,
): DiarySubmissionSummaryDto =
    DiarySubmissionSummaryDto(
        id = submission.id!!,
        date = submission.createdAt,
        lectureTitle = submission.courseTitle,
        shortQuestionReplies = shortQuestionReplies,
        comment = submission.comment,
    )
