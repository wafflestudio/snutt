package com.wafflestudio.snutt.friend.dto

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
        fun from(value: String) = entries.find { it.name == value }
    }
}
