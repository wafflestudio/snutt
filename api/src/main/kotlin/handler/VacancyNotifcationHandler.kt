package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.ExistenceResponse
import com.wafflestudio.snu4t.lectures.dto.LectureDto
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.vacancynotification.dto.VacancyNotificationLecturesResponse
import com.wafflestudio.snu4t.vacancynotification.service.VacancyNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class VacancyNotifcationHandler(
    private val vacancyNotificationService: VacancyNotificationService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiDefaultMiddleware
) {
    suspend fun getVacancyNotificationLectures(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId

        vacancyNotificationService.getVacancyNotificationLectures(userId).map { LectureDto(it) }
            .let { VacancyNotificationLecturesResponse(it) }
    }

    suspend fun existsVacancyNotification(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val lectureId = req.pathVariable("lectureId")
        ExistenceResponse(vacancyNotificationService.existsVacancyNotification(userId, lectureId))
    }

    suspend fun addVacancyNotification(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val lectureId = req.pathVariable("lectureId")

        vacancyNotificationService.addVacancyNotification(userId, lectureId)
        null
    }

    suspend fun deleteVacancyNotification(req: ServerRequest): ServerResponse = handle(req) {
        val lectureId = req.pathVariable("lectureId")

        vacancyNotificationService.deleteVacancyNotification(lectureId)
        null
    }
}
