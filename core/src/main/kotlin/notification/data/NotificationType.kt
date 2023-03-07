package com.wafflestudio.snu4t.notification.data

import com.fasterxml.jackson.annotation.JsonValue

enum class NotificationType(
    @JsonValue
    val value: Int,
) {
    NORMAL(0),
    COURSEBOOK(1),
    LECTURE_UPDATE(2),
    LECTURE_REMOVE(3),
}