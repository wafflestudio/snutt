package com.wafflestudio.snutt.middleware

import com.wafflestudio.snutt.common.exception.WrongApiKeyException
import com.wafflestudio.snutt.handler.RequestContext
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.MacAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import javax.crypto.spec.SecretKeySpec

@Component
class ApiKeyMiddleware(
    @param:Value("\${snutt.secret-key}") private val secretKey: String,
) : Middleware {
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

    override suspend fun invoke(
        req: ServerRequest,
        context: RequestContext,
    ): RequestContext =
        runCatching {
            val apiKey = requireNotNull(req.headers().firstHeader(API_KEY_HEADER))
            val claims = jwtParser.parseSignedClaims(apiKey).payload

            val string = claims["string"]!!.toString()
            val keyVersion = claims["key_version"]!!.toString()
            require(stringToKeyVersionMap[string] == keyVersion)

            return@runCatching context
        }.getOrElse {
            throw WrongApiKeyException
        }
}
