package com.wafflestudio.snutt.filter

import com.wafflestudio.snutt.common.exception.WrongApiKeyException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.MacAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import javax.crypto.spec.SecretKeySpec

/**
 * API Key를 검증하는 필터
 * 모든 요청에 대해 x-access-apikey 헤더를 검증
 */
@Component
@Order(1)
class ApiKeyWebFilter(
    @param:Value("\${snutt.secret-key}") private val secretKey: String,
) : WebFilter {
    companion object {
        const val API_KEY_HEADER = "x-access-apikey"
        const val HMAC_SHA256_ALGORITHM_ID = "HS256"
        const val HMAC_SHA256_JCA_NAME = "HmacSHA256"
        const val DEFAULT_MAC_ALGORITHM_CLASS = "io.jsonwebtoken.impl.security.DefaultMacAlgorithm"
    }

    private val keySpec = SecretKeySpec(secretKey.toByteArray(), HMAC_SHA256_JCA_NAME)

    private val customHS256 =
        run {
            val minKeyBitLength = 80
            Class
                .forName(DEFAULT_MAC_ALGORITHM_CLASS)
                .getDeclaredConstructor(String::class.java, String::class.java, Int::class.java)
                .apply { isAccessible = true }
                .newInstance(HMAC_SHA256_ALGORITHM_ID, HMAC_SHA256_JCA_NAME, minKeyBitLength) as MacAlgorithm
        }

    private val jwtParser =
        Jwts
            .parser()
            .verifyWith(keySpec)
            .sig()
            .remove(Jwts.SIG.HS256)
            .add(customHS256)
            .and()
            .build()

    private val stringToKeyVersionMap =
        mapOf(
            "ios" to "0",
            "web" to "0",
            "android" to "0",
            "test" to "0",
        )

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> =
        runCatching {
            val apiKey =
                exchange.request.headers.getFirst(API_KEY_HEADER)
                    ?: throw WrongApiKeyException

            val claims = jwtParser.parseSignedClaims(apiKey).payload

            val string = claims["string"]?.toString() ?: throw WrongApiKeyException
            val keyVersion = claims["key_version"]?.toString() ?: throw WrongApiKeyException

            if (stringToKeyVersionMap[string] != keyVersion) {
                throw WrongApiKeyException
            }

            chain.filter(exchange)
        }.getOrElse {
            throw WrongApiKeyException
        }
}
