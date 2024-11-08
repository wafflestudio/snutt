package com.wafflestudio.snu4t.auth.apple

import io.jsonwebtoken.Claims

data class AppleUserInfo(
    val sub: String,
    val email: String?,
    val emailVerified: Boolean?,
    val transferSub: String?,
) {
    constructor(jwtPayload: Claims) : this(
        sub = jwtPayload.subject,
        email = jwtPayload["email"] as? String,
        emailVerified = jwtPayload["email_verified"] as? Boolean,
        transferSub = jwtPayload["transfer_sub"] as? String,
    )
}
