package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.handler.RequestContext
import org.springframework.web.reactive.function.server.ServerRequest

interface Middleware {
    fun chain(middleware: Middleware): Middleware
    suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext
}

abstract class BaseMiddleware(
    val func: suspend (req: ServerRequest, context: RequestContext) -> RequestContext,
) : Middleware {
    override fun chain(middleware: Middleware): Middleware =
        DefaultMiddleware { req: ServerRequest, context: RequestContext -> func(req, middleware.invoke(req, context)) }

    override suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext = func(req, context)
}

class DefaultMiddleware(
    func: suspend (req: ServerRequest, context: RequestContext) -> RequestContext =
        { _: ServerRequest, context: RequestContext -> context },
) : BaseMiddleware(func)
