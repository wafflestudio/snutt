package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.exception.InvalidPathParameterException
import com.wafflestudio.snutt.diary.dto.DiaryActivityDto
import com.wafflestudio.snutt.diary.dto.DiaryQuestionDto
import com.wafflestudio.snutt.diary.dto.DiarySubmissionSummaryDto
import com.wafflestudio.snutt.diary.dto.request.DiaryQuestionnaireRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.service.DiaryService
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class DiaryHandler(
    private val diaryService: DiaryService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiDefaultMiddleware,
    ) {
    suspend fun getQuestionnaireFromActivities(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val body = req.awaitBody<DiaryQuestionnaireRequestDto>()

            diaryService.generateQuestionnaire(userId, body.lectureId, body.activities).map {
                DiaryQuestionDto(it)
            }
        }

    suspend fun getMySubmissions(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val year = req.pathVariable("year").toInt()
            val semester =
                Semester.getOfValue(req.pathVariable("semester").toInt()) ?: throw InvalidPathParameterException("semester")
            val submissions = diaryService.getMySubmissions(userId, year, semester)
            val submissionIdShortQuestionRepliesMap = diaryService.getSubmissionIdShortQuestionRepliesMap(submissions)

            submissions.map { submission ->
                DiarySubmissionSummaryDto(submission, submissionIdShortQuestionRepliesMap[submission.id!!]!!)
            }
        }

    suspend fun getActivities(req: ServerRequest) =
        handle(req) {
            diaryService.getActiveActivities().map {
                DiaryActivityDto(it)
            }
        }

    suspend fun submitDiary(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val body = req.awaitBody<DiarySubmissionRequestDto>()

            diaryService.submitDiary(userId, body)
            OkResponse()
        }

    suspend fun removeDiarySubmission(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val id = req.pathVariable("id")

            diaryService.removeSubmission(userId, id)
            OkResponse()
        }
}
