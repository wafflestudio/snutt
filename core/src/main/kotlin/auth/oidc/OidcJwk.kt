package com.wafflestudio.snutt.auth.oidc

data class OidcJwk(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String,
)
