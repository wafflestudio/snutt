package com.wafflestudio.snu4t.auth.google

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleOAuth2UserResponse(
    val id: String,
    val email: String,
    val verifiedEmail: Boolean,
    val name: String?,
    val givenName: String?,
    val familyName: String?,
    val picture: String?,
    val locale: String?,
)
