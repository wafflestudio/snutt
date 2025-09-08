package com.wafflestudio.snutt.auth.apple

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.auth.OAuth2Client
import com.wafflestudio.snutt.auth.OAuth2UserResponse
import com.wafflestudio.snutt.common.exception.InvalidAppleLoginTokenException
import com.wafflestudio.snutt.common.extension.get
import io.jsonwebtoken.Jwts
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.util.Base64

@Component("APPLE")
class AppleClient(
    webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper,
) : OAuth2Client {
    private val webClient =
        webClientBuilder
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().responseTimeout(
                        Duration.ofSeconds(3),
                    ),
                ),
            ).build()

    companion object {
        private const val APPLE_JWK_URI = "https://appleid.apple.com/auth/keys"
    }

    override suspend fun getMe(token: String): OAuth2UserResponse? {
        val jwtHeader = extractJwtHeader(token)
        val appleJwk =
            webClient
                .get<Map<String, List<AppleJwk>>>(uri = APPLE_JWK_URI)
                .getOrNull()
                ?.get("keys")
                ?.find {
                    it.kid == jwtHeader.kid && it.alg == jwtHeader.alg
                } ?: return null
        val publicKey = convertJwkToPublicKey(appleJwk)
        val jwtPayload = verifyAndDecodeToken(token, publicKey)
        val appleUserInfo = AppleUserInfo(jwtPayload)
        return OAuth2UserResponse(
            socialId = appleUserInfo.sub,
            name = null,
            email = appleUserInfo.email,
            isEmailVerified = appleUserInfo.emailVerified ?: true,
            transferInfo = appleUserInfo.transferSub,
        )
    }

    private suspend fun extractJwtHeader(token: String): AppleJwtHeader {
        val headerJson = Base64.getDecoder().decode(token.substringBefore(".")).toString(Charsets.UTF_8)
        val headerMap = objectMapper.readValue(headerJson, Map::class.java)
        val kid = headerMap["kid"] as? String ?: throw InvalidAppleLoginTokenException
        val alg = headerMap["alg"] as? String ?: throw InvalidAppleLoginTokenException
        return AppleJwtHeader(
            kid = kid,
            alg = alg,
        )
    }

    private suspend fun convertJwkToPublicKey(jwk: AppleJwk): PublicKey {
        val modulus = BigInteger(1, Base64.getUrlDecoder().decode(jwk.n))
        val exponent = BigInteger(1, Base64.getUrlDecoder().decode(jwk.e))
        val spec = RSAPublicKeySpec(modulus, exponent)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    private suspend fun verifyAndDecodeToken(
        token: String,
        publicKey: PublicKey,
    ) = Jwts
        .parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .payload
}

private data class AppleJwtHeader(
    val kid: String,
    val alg: String,
)
