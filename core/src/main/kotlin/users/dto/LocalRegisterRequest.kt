package com.wafflestudio.snutt.users.dto

data class LocalRegisterRequest(
    val id: String,
    val password: String,
    val email: String?,
)
