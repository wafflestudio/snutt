package com.wafflestudio.snu4t.friend.dto

import com.wafflestudio.snu4t.users.dto.NicknameDto
import java.time.LocalDateTime

data class FriendResponse(
    val id: String,
    val userId: String,
    val nickname: NicknameDto,
    val createdAt: LocalDateTime,
)
