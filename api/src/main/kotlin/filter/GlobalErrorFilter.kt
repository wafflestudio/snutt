package com.wafflestudio.snu4t.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snu4t.common.exception.Snu4tException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class GlobalErrorFilter : WebFilter {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .onErrorResume { ex ->
                val errorBody = if (ex is Snu4tException) {
                    ErrorBody(ex)
                } else {
                    logger.error(ex.message)
                    ErrorBody(Snu4tException())
                }

                exchange.response.rawStatusCode = HttpStatus.BAD_GATEWAY.value()
                exchange.response.writeWith(
                    Flux.just(
                        exchange.response
                            .bufferFactory()
                            .wrap(mapper.writeValueAsBytes(errorBody))
                    )
                )
            }
    }

    data class ErrorBody(val errorCode: Long, val message: String, val displayMessage: String)

    private fun ErrorBody(exception: Snu4tException): ErrorBody =
        ErrorBody(exception.error.errorCode, exception.errorMessage, exception.displayMessage)
}
