package com.wafflestudio.snutt.users.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PasswordResetRequest(
    val userId: String,
    val password: String,
    val code: String,
)
