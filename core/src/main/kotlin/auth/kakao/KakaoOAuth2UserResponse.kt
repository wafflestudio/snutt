package com.wafflestudio.snutt.auth.kakao

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

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
