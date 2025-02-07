package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FacebookLoginRequest(
    val fbId: String?,
    val fbToken: String,
)
