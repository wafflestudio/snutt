package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PasswordResetRequest(
    @param:JsonProperty("user_id")
    val userId: String,
    val password: String,
    val code: String,
)
