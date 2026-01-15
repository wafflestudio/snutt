package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LogoutRequest(
    @param:JsonProperty("registration_id")
    val registrationId: String,
)
