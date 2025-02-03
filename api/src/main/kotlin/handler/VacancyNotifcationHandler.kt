package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.ExistenceResponse
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.vacancynotification.dto.VacancyNotificationLecturesResponse
import com.wafflestudio.snutt.vacancynotification.service.VacancyNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class VacancyNotifcationHandler(
    private val vacancyNotificationService: VacancyNotificationService,
    private val lectureService: LectureService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiDefaultMiddleware,
    ) {
    suspend fun getVacancyNotificationLectures(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId

            vacancyNotificationService.getVacancyNotificationLectures(userId)
                .let { lectureService.convertLecturesToLectureDtos(it) }
                .let { VacancyNotificationLecturesResponse(it) }
        }

    suspend fun existsVacancyNotification(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val lectureId = req.pathVariable("lectureId")
            ExistenceResponse(vacancyNotificationService.existsVacancyNotification(userId, lectureId))
        }

    suspend fun addVacancyNotification(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val lectureId = req.pathVariable("lectureId")

            vacancyNotificationService.addVacancyNotification(userId, lectureId)
            null
        }

    suspend fun deleteVacancyNotification(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val lectureId = req.pathVariable("lectureId")

            vacancyNotificationService.deleteVacancyNotification(userId, lectureId)
            null
        }
}
