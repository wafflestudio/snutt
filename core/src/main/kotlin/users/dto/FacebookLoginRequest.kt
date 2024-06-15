package com.wafflestudio.snu4t.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FacebookLoginRequest(
    val token: String,
    val userId: String?,
    val email: String?,
)
