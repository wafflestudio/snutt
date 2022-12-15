package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.exception.Snu4tException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class ErrorHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    suspend fun handle(throwable: Throwable): ServerResponse {

        return when (throwable) {
            is Snu4tException -> ServerResponse.status(HttpStatus.BAD_GATEWAY)
                .bodyValueAndAwait(makeErrorBody(throwable))

            else -> {
                logger.error(throwable.message)
                ServerResponse.status(HttpStatus.BAD_GATEWAY).bodyValueAndAwait(makeErrorBody(Snu4tException()))
            }
        }
    }

    private fun makeErrorBody(
        exception: Snu4tException,
    ): ErrorBody {
        return ErrorBody(exception.error.errorCode, exception.errorMessage, exception.displayMessage)
    }
}

data class ErrorBody(val errorCode: Long, val message: String, val displayMessage: String)
