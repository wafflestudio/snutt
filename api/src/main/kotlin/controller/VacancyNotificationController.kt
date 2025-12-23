package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.dto.ExistenceResponse
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.lectures.service.LectureService
import com.wafflestudio.snutt.users.data.User
import com.wafflestudio.snutt.vacancynotification.dto.VacancyNotificationLecturesResponse
import com.wafflestudio.snutt.vacancynotification.service.VacancyNotificationService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("/v1/vacancy-notifications", "/vacancy-notifications")
class VacancyNotificationController(
    private val vacancyNotificationService: VacancyNotificationService,
    private val lectureService: LectureService,
) {
    @GetMapping("/lectures")
    suspend fun getVacancyNotificationLectures(
        @CurrentUser user: User,
    ) = vacancyNotificationService
        .getVacancyNotificationLectures(user.id!!)
        .let { lectureService.convertLecturesToLectureDtos(it) }
        .let { VacancyNotificationLecturesResponse(it) }

    @GetMapping("/lectures/{lectureId}/state")
    suspend fun existsVacancyNotification(
        @CurrentUser user: User,
        @PathVariable lectureId: String,
    ) = ExistenceResponse(vacancyNotificationService.existsVacancyNotification(user.id!!, lectureId))

    @PostMapping("/lectures/{lectureId}")
    suspend fun addVacancyNotification(
        @CurrentUser user: User,
        @PathVariable lectureId: String,
    ) {
        vacancyNotificationService.addVacancyNotification(user.id!!, lectureId)
    }

    @DeleteMapping("/lectures/{lectureId}")
    suspend fun deleteVacancyNotification(
        @CurrentUser user: User,
        @PathVariable lectureId: String,
    ) {
        vacancyNotificationService.deleteVacancyNotification(user.id!!, lectureId)
    }
}
