package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.handler.RequestContext
import com.wafflestudio.snu4t.users.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class UserAuthrizeMiddleware(private val userService: UserService) : BaseMiddleware({ req: ServerRequest, context: RequestContext ->
    val token = req.headers().firstHeader("x-access-token") ?: throw AuthException
    val currentUser = userService.getUserByCredentialHash(token)
    context.apply { user = currentUser }
})
