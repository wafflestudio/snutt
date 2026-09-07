package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.common.extension.toZonedDateTime
import com.wafflestudio.snutt.diary.data.DiarySubmission
import java.time.ZonedDateTime

data class DiarySubmissionSummaryDto(
    val id: String,
    val lectureId: String,
    val date: ZonedDateTime,
    val courseTitle: String,
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
        lectureId = submission.lectureId,
        date = submission.createdAt.toZonedDateTime(),
        courseTitle = submission.courseTitle,
        shortQuestionReplies = shortQuestionReplies,
        comment = submission.comment,
    )
