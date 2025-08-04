package com.wafflestudio.snutt.middleware

import com.wafflestudio.snutt.common.exception.WrongApiKeyException
import com.wafflestudio.snutt.handler.RequestContext
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import javax.crypto.spec.SecretKeySpec

@Component
class ApiKeyMiddleware(
    @Value("\${snutt.secret-key}") private val secretKey: String,
) : Middleware {
    private val keySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
    private val jwtParser = Jwts.parser().verifyWith(keySpec).build()
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
            val apiKey = requireNotNull(req.headers().firstHeader("x-access-apikey"))
            val claims = jwtParser.parseSignedClaims(apiKey).payload

            val string = claims["string"]!!.toString()
            val keyVersion = claims["key_version"]!!.toString()
            require(stringToKeyVersionMap[string] == keyVersion)

            return@runCatching context
        }.getOrElse {
            throw WrongApiKeyException
        }
}
