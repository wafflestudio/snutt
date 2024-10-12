package com.wafflestudio.snu4t.friend.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class FriendLink(
    @Id
    val id: String? = null,
    val fromUserId: String,
    @Indexed(name = "ttl", expireAfter = "14d")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
