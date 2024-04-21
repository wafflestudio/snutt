package com.wafflestudio.snu4t.friend.dto

import java.time.LocalDateTime

data class FriendRequestLinkResponse(
    val requestInfo: String,
    val expireAt: LocalDateTime
)
