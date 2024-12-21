package com.wafflestudio.snu4t.evaluation.dto

import com.wafflestudio.snu4t.users.data.User

data class EvUserDto(
    val id: String?,
    val email: String?,
    val local_id: String?,
)

fun EvUserDto(user: User) =
    EvUserDto(
        id = user.id,
        email = user.email,
        local_id = user.credential.localId,
    )
