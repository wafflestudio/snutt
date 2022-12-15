package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.handler.RequestContext
import org.springframework.web.reactive.function.server.ServerRequest

interface Middleware {
    infix fun chain(middleware: Middleware): Middleware
    suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext
}

abstract class BaseMiddleware(
    val func: suspend (req: ServerRequest, context: RequestContext) -> RequestContext,
) : Middleware {
    override infix fun chain(middleware: Middleware): Middleware =
        DefaultMiddleware { req, context -> func(req, middleware.invoke(req, context)) }

    override suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext = func(req, context)
}

class DefaultMiddleware(
    func: suspend (req: ServerRequest, context: RequestContext) -> RequestContext,
) : BaseMiddleware(func)

object NoOpMiddleWare : BaseMiddleware({ _, context -> context })
