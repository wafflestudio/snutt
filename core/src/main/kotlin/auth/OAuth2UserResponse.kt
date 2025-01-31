package com.wafflestudio.snutt.auth

data class OAuth2UserResponse(
    val socialId: String,
    val name: String?,
    val email: String?,
    val isEmailVerified: Boolean,
    val transferInfo: String? = null,
)
