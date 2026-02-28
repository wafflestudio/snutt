package com.wafflestudio.snutt.auth.apple

import com.wafflestudio.snutt.auth.OAuth2Client
import com.wafflestudio.snutt.auth.OAuth2UserResponse
import com.wafflestudio.snutt.auth.oidc.OidcJwtVerifier
import com.wafflestudio.snutt.auth.oidc.OidcVerificationOptions
import com.wafflestudio.snutt.common.exception.InvalidAppleLoginTokenException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component("APPLE")
class AppleClient(
    private val oidcJwtVerifier: OidcJwtVerifier,
    @param:Value("\${oidc.apple.app-id:}") private val appleAppId: String,
) : OAuth2Client {
    companion object {
        private const val APPLE_JWK_URI = "https://appleid.apple.com/auth/keys"
        private const val APPLE_ISSUER = "https://appleid.apple.com"
    }

    override suspend fun getMe(token: String): OAuth2UserResponse? {
        if (!oidcJwtVerifier.looksLikeJwt(token)) throw InvalidAppleLoginTokenException

        val jwtPayload =
            oidcJwtVerifier.verifyAndDecodeToken(
                token = token,
                options =
                    OidcVerificationOptions(
                        jwksUri = APPLE_JWK_URI,
                        expectedIssuer = APPLE_ISSUER,
                        expectedAudience = appleAppId,
                    ),
            ) ?: return null

        val appleUserInfo = AppleUserInfo(jwtPayload)
        return OAuth2UserResponse(
            socialId = appleUserInfo.sub,
            name = null,
            email = appleUserInfo.email,
            isEmailVerified = appleUserInfo.emailVerified ?: true,
            transferInfo = appleUserInfo.transferSub,
        )
    }
}
