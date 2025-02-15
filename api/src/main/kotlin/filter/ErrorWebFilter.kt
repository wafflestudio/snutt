package com.wafflestudio.snutt.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.common.exception.ErrorType
import com.wafflestudio.snutt.common.exception.EvServiceProxyException
import com.wafflestudio.snutt.common.exception.SnuttException
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.netty.channel.AbortedException

@Component
class ErrorWebFilter(
    private val objectMapper: ObjectMapper,
) : WebFilter {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        return chain.filter(exchange)
            .onErrorResume { throwable ->
                val errorBody: Any
                val httpStatusCode: HttpStatusCode
                when (throwable) {
                    is EvServiceProxyException -> {
                        httpStatusCode = throwable.statusCode
                        errorBody = throwable.errorBody
                    }
                    is SnuttException -> {
                        httpStatusCode = throwable.error.httpStatus
                        errorBody = makeErrorBody(throwable)
                    }
                    is ResponseStatusException -> {
                        httpStatusCode = throwable.statusCode
                        errorBody =
                            makeErrorBody(
                                SnuttException(errorMessage = throwable.body.title ?: ErrorType.DEFAULT_ERROR.errorMessage),
                            )
                    }
                    is AbortedException, is CancellationException -> {
                        httpStatusCode = HttpStatus.NO_CONTENT
                        errorBody = emptyMap<String, Any>()
                    }
                    else -> {
                        log.error(throwable.message, throwable)
                        httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        errorBody = makeErrorBody(SnuttException())
                    }
                }

                exchange.response.statusCode = httpStatusCode
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                exchange.response.writeWith(
                    Mono.just(
                        exchange.response
                            .bufferFactory()
                            .wrap(objectMapper.writeValueAsBytes(errorBody)),
                    ),
                )
            }
    }

    private fun makeErrorBody(exception: SnuttException): ErrorBody {
        return ErrorBody(exception.error.errorCode, exception.errorMessage, exception.displayMessage, exception.ext)
    }
}

private data class ErrorBody(
    val errcode: Long,
    val message: String,
    val displayMessage: String,
    // TODO: 구버전 대응용 ext 필드. 추후 삭제
    val ext: Map<String, String> = mapOf(),
)
