package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.handler.RequestContext
import org.springframework.web.reactive.function.server.ServerRequest

fun interface Middleware {
    suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext
}

operator fun Middleware.plus(other: Middleware): Middleware =
    Middleware { req, context -> this.invoke(req, context).let { other.invoke(req, it) } }

object NoOpMiddleWare : Middleware {
    override suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext = context
}
