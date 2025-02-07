package com.wafflestudio.snutt.auth

interface OAuth2Client {
    suspend fun getMe(token: String): OAuth2UserResponse?
}
