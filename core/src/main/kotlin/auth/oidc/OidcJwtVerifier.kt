package com.wafflestudio.snutt.auth.oidc

import com.wafflestudio.snutt.common.extension.get
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.util.Base64
import java.util.Date

data class OidcVerificationOptions(
    val jwksUri: String,
    val expectedIssuer: String? = null,
    val expectedAudience: String? = null,
)

@Component
@RegisterReflectionForBinding(
    OidcJwkSet::class,
    OidcJwk::class,
)
class OidcJwtVerifier(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient =
        WebClient
            .builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().responseTimeout(
                        Duration.ofSeconds(3),
                    ),
                ),
            ).build()

    suspend fun verifyAndDecodeToken(
        token: String,
        options: OidcVerificationOptions,
    ): Claims? =
        runCatching {
            val jwtHeader = extractJwtHeader(token) ?: return null
            val oidcJwk = fetchJwk(jwtHeader, options.jwksUri) ?: return null
            val publicKey = convertJwkToPublicKey(oidcJwk)
            val claims = parseSignedClaims(token, publicKey)

            if (!isValidIssuer(claims, options.expectedIssuer)) return null
            if (!isValidAudience(claims, options.expectedAudience)) return null
            if (!isNotExpired(claims)) return null

            claims
        }.onFailure {
            log.warn("failed to verify oidc token {}: {}", token, it.message)
        }.getOrNull()

    fun looksLikeJwt(token: String): Boolean {
        val parts = token.split(".")
        return parts.size == 3 && parts.none { it.isBlank() }
    }

    private suspend fun fetchJwk(
        jwtHeader: OidcJwtHeader,
        jwksUri: String,
    ): OidcJwk? =
        webClient
            .get<OidcJwkSet>(uri = jwksUri)
            .getOrNull()
            ?.keys
            ?.find {
                it.kid == jwtHeader.kid && (it.alg == jwtHeader.alg || it.alg.isBlank())
            } ?: return null

    private fun extractJwtHeader(token: String): OidcJwtHeader? {
        if (!looksLikeJwt(token)) return null

        val headerJson = Base64.getUrlDecoder().decode(token.substringBefore(".")).toString(Charsets.UTF_8)
        val headerMap: Map<String, String?> = objectMapper.readValue(headerJson)
        val kid = headerMap["kid"] ?: return null
        val alg = headerMap["alg"] ?: return null

        return OidcJwtHeader(
            kid = kid,
            alg = alg,
        )
    }

    private fun convertJwkToPublicKey(jwk: OidcJwk): PublicKey {
        if (jwk.kty != "RSA") throw IllegalArgumentException("unsupported kty: ${jwk.kty}")

        val modulus = BigInteger(1, Base64.getUrlDecoder().decode(jwk.n))
        val exponent = BigInteger(1, Base64.getUrlDecoder().decode(jwk.e))
        val spec = RSAPublicKeySpec(modulus, exponent)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    private fun parseSignedClaims(
        token: String,
        publicKey: PublicKey,
    ) = Jwts
        .parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .payload

    private fun isValidIssuer(
        claims: Claims,
        expectedIssuer: String?,
    ): Boolean = expectedIssuer == null || (claims["iss"] as? String) == expectedIssuer

    private fun isValidAudience(
        claims: Claims,
        expectedAudience: String?,
    ): Boolean {
        if (expectedAudience == null) return true

        val audience = claims["aud"] ?: return false

        return when (audience) {
            is String -> audience == expectedAudience
            is Collection<*> -> audience.any { it == expectedAudience }
            else -> false
        }
    }

    private fun isNotExpired(claims: Claims): Boolean {
        val expiration = claims.expiration ?: return false
        return expiration.after(Date())
    }
}

private data class OidcJwtHeader(
    val kid: String,
    val alg: String,
)
