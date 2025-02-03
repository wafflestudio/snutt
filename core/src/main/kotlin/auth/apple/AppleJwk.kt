package com.wafflestudio.snutt.auth.apple

data class AppleJwk(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String,
)
