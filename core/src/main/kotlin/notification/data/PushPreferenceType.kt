package com.wafflestudio.snutt.notification.data

enum class PushPreferenceType(val isEnabledByDefault: Boolean) {
    NORMAL(true),
    LECTURE_UPDATE(true),
    VACANCY_NOTIFICATION(true),
}

fun PushPreferenceType(notificationType: NotificationType) =
    when (notificationType) {
        NotificationType.LECTURE_UPDATE -> PushPreferenceType.LECTURE_UPDATE
        NotificationType.LECTURE_VACANCY -> PushPreferenceType.VACANCY_NOTIFICATION
        else -> PushPreferenceType.NORMAL
    }
