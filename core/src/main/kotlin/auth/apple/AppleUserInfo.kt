package com.wafflestudio.snutt.auth.apple

import io.jsonwebtoken.Claims

data class AppleUserInfo(
    val sub: String,
    val email: String?,
    val emailVerified: Boolean?,
    val transferSub: String?,
)

fun AppleUserInfo(jwtPayload: Claims): AppleUserInfo =
    AppleUserInfo(
        sub = jwtPayload.subject,
        email = jwtPayload["email"] as? String,
        emailVerified = jwtPayload["email_verified"] as? Boolean,
        transferSub = jwtPayload["transfer_sub"] as? String,
    )
