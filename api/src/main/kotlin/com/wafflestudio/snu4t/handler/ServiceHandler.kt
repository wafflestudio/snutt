package com.wafflestudio.snu4t.handler

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.server.ServerResponse

abstract class ServiceHandler {
    @Autowired
    lateinit var errorHandler: ErrorHandler

    protected suspend fun handle(function: suspend () -> ServerResponse): ServerResponse {
        return try {
            function()
        } catch (e: RuntimeException) {
            errorHandler.handle(e)
        }
    }

    protected suspend fun <T : Any> T.toResponse(): ServerResponse {
        return ServerResponse.ok().bodyValue(this).awaitSingle()
    }
}