package com.wafflestudio.snutt.friend.dto

import com.wafflestudio.snutt.users.dto.NicknameDto
import java.time.LocalDateTime

data class FriendResponse(
    val id: String,
    val userId: String,
    val displayName: String?,
    val nickname: NicknameDto,
    val createdAt: LocalDateTime,
)
