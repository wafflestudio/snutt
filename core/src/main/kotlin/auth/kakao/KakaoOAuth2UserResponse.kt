package com.wafflestudio.snu4t.auth.kakao

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoOAuth2UserResponse(
    val id: Long,
    val kakaoAccount: KakaoAccountDto,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoAccountDto(
    val email: String,
    val isEmailVerified: Boolean,
)
