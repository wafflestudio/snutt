package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class VerificationCodeRequest(
    @JsonProperty("user_id")
    val userId: String? = null,
    val code: String,
)
