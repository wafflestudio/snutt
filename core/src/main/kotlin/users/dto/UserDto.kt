package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class UserDto(
    val id: String,
    val isAdmin: Boolean,
    val regDate: LocalDateTime,
    val notificationCheckedAt: LocalDateTime,
    val email: String?,
    val localId: String?,
    val fbName: String?,
    val nickname: NicknameDto,
)

data class UserLegacyDto(
    val isAdmin: Boolean,
    val regDate: ZonedDateTime,
    val notificationCheckedAt: ZonedDateTime,
    val email: String?,
    @JsonProperty("local_id")
    val localId: String?,
    @JsonProperty("fb_name")
    val fbName: String?,
)
