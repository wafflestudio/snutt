package com.wafflestudio.snu4t.notification.dto

import com.wafflestudio.snu4t.users.data.User

data class NotificationQuery(
    val offset: Int,
    val limit: Int,
    val explicit: Boolean,
    val user: User,
)
