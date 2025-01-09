package com.wafflestudio.snu4t.evaluation.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.users.data.User

data class EvUserDto(
    val id: String?,
    val email: String?,
    @JsonProperty("local_id")
    val localId: String?,
)

fun EvUserDto(user: User) =
    EvUserDto(
        id = user.id,
        email = user.email,
        localId = user.credential.localId,
    )
