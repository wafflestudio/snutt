package com.wafflestudio.snutt.users.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class SocialLoginRequest(
    @JsonAlias("fb_token", "apple_token")
    val token: String,
)
