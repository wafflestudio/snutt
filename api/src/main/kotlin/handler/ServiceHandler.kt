package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.exception.InvalidParameterException
import com.wafflestudio.snu4t.common.exception.MissingRequiredParameterException
import com.wafflestudio.snu4t.middleware.Middleware
import com.wafflestudio.snu4t.middleware.plus
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull

abstract class ServiceHandler(val handlerMiddleware: Middleware = Middleware.NoOp) {
    protected suspend fun <T : Any> handle(
        req: ServerRequest,
        additionalMiddleware: Middleware = Middleware.NoOp,
        function: suspend () -> T?,
    ): ServerResponse {
        req.setContext((handlerMiddleware + additionalMiddleware).invoke(req, req.getContext()))
        return function()?.toResponse() ?: ServerResponse.ok().buildAndAwait() // Unit ? null?
    }

    private suspend fun <T : Any> T.toResponse(): ServerResponse {
        return ServerResponse.ok().bodyValue(this).awaitSingle()
    }

    fun <T> ServerRequest.parseRequiredQueryParam(name: String, convert: (String) -> T?): T =
        parseQueryParam(name, convert)
            ?: throw MissingRequiredParameterException(name)

    fun <T> ServerRequest.parseQueryParam(name: String, convert: (String) -> T?): T? =
        this.queryParamOrNull(name)?.runCatching { convert(this)!! }
            ?.getOrElse { throw InvalidParameterException(name) }

    inline fun <reified T> ServerRequest.parseRequiredQueryParam(name: String): T =
        parseQueryParam<T>(name)
            ?: throw MissingRequiredParameterException(name)

    inline fun <reified T> ServerRequest.parseQueryParam(name: String): T? {
        return when (T::class) {
            String::class -> this.parseQueryParam(name) { it } as T?
            Int::class -> this.parseQueryParam(name) { it.toIntOrNull() } as T?
            Long::class -> this.parseQueryParam(name) { it.toLongOrNull() } as T?
            Float::class -> this.parseQueryParam(name) { it.toFloatOrNull() } as T?
            Double::class -> this.parseQueryParam(name) { it.toDoubleOrNull() } as T?
            Boolean::class -> this.parseQueryParam(name) { it.toBooleanStrictOrNull() } as T?
            else -> throw IllegalArgumentException("파싱을 지원하지 않는 쿼리 파라미터 타입입니다. type: ${T::class}")
        }
    }
}
