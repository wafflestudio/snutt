package com.wafflestudio.snutt.middleware

import com.wafflestudio.snutt.handler.RequestContext
import org.springframework.web.reactive.function.server.ServerRequest

fun interface Middleware {
    suspend fun invoke(
        req: ServerRequest,
        context: RequestContext,
    ): RequestContext

    companion object {
        val NoOp = Middleware { _, context -> context }
    }
}

operator fun Middleware.plus(other: Middleware): Middleware =
    Middleware { req, context -> this.invoke(req, context).let { other.invoke(req, it) } }
