package com.wafflestudio.snu4t.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PasswordResetRequest(
    @JsonProperty("old_password")
    val oldPassword: String,
    @JsonProperty("new_password")
    val newPassword: String,
)
