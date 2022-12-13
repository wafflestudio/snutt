package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.DefaultMiddleware
import com.wafflestudio.snu4t.middleware.Middleware
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

abstract class ServiceHandler(val handlerMiddleware: Middleware = DefaultMiddleware()) {
    @Autowired
    lateinit var errorHandler: ErrorHandler

    protected suspend fun handle(
        req: ServerRequest,
        additionalMiddleware: Middleware = DefaultMiddleware(),
        function: suspend () -> ServerResponse,
    ): ServerResponse {
        req.setContext(handlerMiddleware.chain(additionalMiddleware).invoke(req, req.getContext()))
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
