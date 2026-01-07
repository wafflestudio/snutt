package com.wafflestudio.snutt.users.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FacebookLoginRequest(
    val fbId: String?,
    val fbToken: String,
)
