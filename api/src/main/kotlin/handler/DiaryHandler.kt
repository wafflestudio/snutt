package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.diary.dto.DiaryActivityDto
import com.wafflestudio.snutt.diary.dto.DiaryQuestionnaireDto
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

            DiaryQuestionnaireDto(diaryService.generateQuestionnaire(userId, body.lectureId, body.activities))
        }

    suspend fun getMySubmissions(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val submissions = diaryService.getMySubmissions(userId)
            val submissionIdShortQuestionRepliesMap = diaryService.getSubmissionIdShortQuestionRepliesMap(submissions)

            submissions
                .groupBy { submission ->
                    submission.year
                }.mapValues {
                    it.value
                        .groupBy { submission ->
                            submission.semester.value
                        }.mapValues { it2 ->
                            it2.value.map { submission ->
                                DiarySubmissionSummaryDto(submission, submissionIdShortQuestionRepliesMap[submission.id!!]!!)
                            }
                        }
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
