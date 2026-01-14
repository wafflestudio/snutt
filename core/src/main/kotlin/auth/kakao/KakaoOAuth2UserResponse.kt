package com.wafflestudio.snutt.auth.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoOAuth2UserResponse(
    val id: Long,
    @param:JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccountDto,
)

data class KakaoAccountDto(
    val email: String,
    @param:JsonProperty("is_email_verified")
    val isEmailVerified: Boolean,
)
