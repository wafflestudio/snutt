package com.wafflestudio.snu4t.vacancynotification.dto

import com.wafflestudio.snu4t.vacancynotification.data.VacancyNotification

data class VacancyNotificationDto(
    val id: String,
    val userId: String,
    val lectureId: String,
)

fun VacancyNotificationDto(vacancyNotification: VacancyNotification) = VacancyNotificationDto(
    id = vacancyNotification.id!!,
    userId = vacancyNotification.userId,
    lectureId = vacancyNotification.lectureId,
)
