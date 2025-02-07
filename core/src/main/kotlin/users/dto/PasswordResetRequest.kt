package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PasswordResetRequest(
    val userId: String,
    val password: String,
    val code: String,
)
