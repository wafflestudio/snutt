package com.wafflestudio.snutt.users.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String,
)
