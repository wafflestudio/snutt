package com.wafflestudio.snu4t.auth

interface OAuth2Client {
    suspend fun getMe(
        token: String,
    ): OAuth2UserResponse?
}
