package com.wafflestudio.snutt.auth.facebook

data class FacebookOAuth2UserResponse(
    val id: String,
    val email: String?,
    val name: String?,
)
