package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PasswordChangeRequest(
    @param:JsonProperty("old_password")
    val oldPassword: String,
    @param:JsonProperty("new_password")
    val newPassword: String,
)
