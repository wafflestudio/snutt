package com.wafflestudio.snu4t.friend.dto

import java.time.LocalDateTime

data class FriendResponse(
    val id: String,
    val userId: String,
    val nickname: NicknameResponse,
    val createdAt: LocalDateTime,
)

data class NicknameResponse(
    val nickname: String,
    val tag: String,
)
