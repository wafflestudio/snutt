package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class SendEmailRequest(
    @JsonProperty("email")
    @JsonAlias("user_email")
    val email: String,
)
