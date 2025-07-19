package com.wafflestudio.snutt.handler

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
    suspend fun getQuestionnaireFromActivityTypes(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val body = req.awaitBody<DiaryQuestionnaireRequestDto>()

            diaryService.generateQuestionnaire(userId, body.lectureId, body.activityTypes)
        }

    suspend fun getMySubmissions(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            diaryService.getMySubmissions(userId)
        }

    suspend fun getActivityTypes(req: ServerRequest) =
        handle(req) {
            diaryService.getActiveActivityTypes()
        }

    suspend fun submitDiary(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val body = req.awaitBody<DiarySubmissionRequestDto>()

            diaryService.submitDiary(userId, body)
        }
}
