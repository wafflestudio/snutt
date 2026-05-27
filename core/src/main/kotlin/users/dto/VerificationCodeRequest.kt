package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class VerificationCodeRequest(
    @param:JsonProperty("user_id")
    val localId: String? = null,
    val code: String,
)
