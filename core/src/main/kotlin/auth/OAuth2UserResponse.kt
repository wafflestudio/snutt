package com.wafflestudio.snu4t.auth

data class OAuth2UserResponse(
    val socialId: String,
    val name: String?,
    val email: String?,
    val isEmailVerified: Boolean,
)
