package com.wafflestudio.snu4t.friend.dto

import java.time.LocalDateTime

data class FriendResponse(
    val id: String,
    val userId: String,
    val nickname: String,
    val createdAt: LocalDateTime,
)
