package com.wafflestudio.snu4t.vacancynotification.dto

import com.wafflestudio.snu4t.lectures.dto.LectureDto

data class VacancyNotificationLecturesResponse(
    val lectures: List<LectureDto>
)
