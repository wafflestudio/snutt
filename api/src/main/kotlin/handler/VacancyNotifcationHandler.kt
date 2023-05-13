package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.vacancynotification.dto.VacancyNotificationDto
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
    suspend fun addVacancyNotification(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val lectureId = req.pathVariable("lectureId")

        vacancyNotificationService.addVacancyNotification(userId, lectureId)
            .let { VacancyNotificationDto(it) }
    }

    suspend fun getVacancyNotifications(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId

        vacancyNotificationService.getVacancyNotifications(userId)
    }

    suspend fun getVacancyNotification(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val lectureId = req.pathVariable("lectureId")

        vacancyNotificationService.getVacancyNotification(userId, lectureId)
    }
    suspend fun deleteVacancyNotification(req: ServerRequest): ServerResponse = handle(req) {
        val id = req.pathVariable("id")

        vacancyNotificationService.deleteVacancyNotification(id)
        null
    }
}
