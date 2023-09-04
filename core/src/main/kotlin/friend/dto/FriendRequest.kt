package com.wafflestudio.snu4t.friend.dto

data class FriendRequest(
    val nickname: String,
)

data class UpdateFriendDisplayNameRequest(
    val displayName: String,
)

enum class FriendState {
    ACTIVE,
    REQUESTING,
    REQUESTED,
    ;

    companion object {
        fun from(value: String) = values().find { it.name == value }
    }
}
