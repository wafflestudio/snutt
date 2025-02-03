package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginResponse(
    val userId: String,
    val token: String,
    val message: String = "ok",
)
