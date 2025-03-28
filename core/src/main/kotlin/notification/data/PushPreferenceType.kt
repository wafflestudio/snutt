package com.wafflestudio.snutt.notification.data

enum class PushPreferenceType {
    NORMAL,
    LECTURE_UPDATE,
    VACANCY_NOTIFICATION,
}

fun PushPreferenceType(notificationType: NotificationType) =
    when (notificationType) {
        NotificationType.LECTURE_UPDATE -> PushPreferenceType.LECTURE_UPDATE
        NotificationType.LECTURE_VACANCY -> PushPreferenceType.VACANCY_NOTIFICATION
        else -> PushPreferenceType.NORMAL
    }
