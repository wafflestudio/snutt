package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(
    @param:JsonProperty("user_id")
    val userId: String,
    val token: String,
    val message: String = "ok",
)
