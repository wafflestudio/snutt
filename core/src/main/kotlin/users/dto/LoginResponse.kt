package com.wafflestudio.snu4t.users.dto

data class LoginResponse(
    val userId: String,
    val token: String,
    val message: String = "ok",
)
