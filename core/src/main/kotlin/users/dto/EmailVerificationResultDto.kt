package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EmailVerificationResultDto(
    @JsonProperty("is_email_verified")
    val isEmailVerified: Boolean = false,
)
