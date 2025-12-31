package com.wafflestudio.snutt.filter

import com.wafflestudio.snutt.common.exception.ErrorType
import com.wafflestudio.snutt.common.exception.EvServiceProxyException
import com.wafflestudio.snutt.common.exception.SnuttException
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
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
import tools.jackson.databind.ObjectMapper

@Component
@Order(0)
class ErrorWebFilter(
    private val objectMapper: ObjectMapper,
) : WebFilter {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> =
        chain
            .filter(exchange)
            .onErrorResume { throwable ->
                val errorBody: ErrorBody
                val httpStatusCode: HttpStatusCode
                when (throwable) {
                    is EvServiceProxyException -> {
                        httpStatusCode = throwable.statusCode
                        errorBody =
                            throwable.errorResponse.let {
                                ErrorBody(
                                    it.error.code,
                                    "",
                                    it.error.message,
                                    it.error.message,
                                )
                            }
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
                        errorBody = makeErrorBody(SnuttException())
                    }
                    else -> {
                        log.error(throwable.message, throwable)
                        httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        errorBody = makeErrorBody(SnuttException())
                    }
                }

                if (!exchange.response.isCommitted) {
                    exchange.response.statusCode = httpStatusCode
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    exchange.response.writeWith(
                        Mono.just(
                            exchange.response
                                .bufferFactory()
                                .wrap(objectMapper.writeValueAsBytes(errorBody)),
                        ),
                    )
                } else {
                    Mono.empty()
                }
            }

    private fun makeErrorBody(exception: SnuttException): ErrorBody =
        ErrorBody(exception.error.errorCode, exception.title, exception.errorMessage, exception.displayMessage)
}

private data class ErrorBody(
    val errcode: Long,
    val title: String,
    val message: String,
    val displayMessage: String,
)
