package com.wafflestudio.snutt.diary.dto

import com.wafflestudio.snutt.common.extension.toZonedDateTime
import com.wafflestudio.snutt.diary.data.DiarySubmission
import io.swagger.v3.oas.annotations.media.Schema
import java.time.format.DateTimeFormatter

data class DiarySubmissionSummaryDto(
    val id: String,
    val lectureId: String,
    @field:Schema(type = "string", format = "date-time")
    val date: String,
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
    useLegacyDateFormat: Boolean = false,
): DiarySubmissionSummaryDto =
    DiarySubmissionSummaryDto(
        id = submission.id!!,
        lectureId = submission.lectureId,
        date =
            if (useLegacyDateFormat) {
                submission.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } else {
                submission.createdAt.toZonedDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            },
        courseTitle = submission.courseTitle,
        shortQuestionReplies = shortQuestionReplies,
        comment = submission.comment,
    )
