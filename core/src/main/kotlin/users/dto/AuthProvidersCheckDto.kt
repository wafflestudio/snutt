package com.wafflestudio.snu4t.users.dto

data class AuthProvidersCheckDto(
    val local: Boolean,
    val facebook: Boolean,
    val google: Boolean,
    val kakao: Boolean,
    val apple: Boolean,
)
