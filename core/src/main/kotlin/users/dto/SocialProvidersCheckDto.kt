package com.wafflestudio.snu4t.users.dto

data class SocialProvidersCheckDto(
    val local: Boolean,
    val facebook: Boolean,
    val google: Boolean,
    val kakao: Boolean,
    val apple: Boolean,
)
