package com.wafflestudio.snutt.notification.dto

import com.wafflestudio.snutt.users.data.User

data class NotificationQuery(
    val offset: Long,
    val limit: Int,
    val explicit: Boolean,
    val user: User,
)
