package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GetMaskedEmailRequest(
    @JsonProperty("user_id")
    val userId: String,
)
