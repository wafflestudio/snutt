package com.wafflestudio.snutt.users.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginResponse(
    val userId: String,
    val token: String,
    val message: String = "ok",
)
