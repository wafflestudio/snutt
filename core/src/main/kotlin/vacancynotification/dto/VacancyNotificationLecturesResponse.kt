package com.wafflestudio.snutt.vacancynotification.dto

import com.wafflestudio.snutt.lectures.dto.LectureDto

data class VacancyNotificationLecturesResponse(
    val lectures: List<LectureDto>,
)
