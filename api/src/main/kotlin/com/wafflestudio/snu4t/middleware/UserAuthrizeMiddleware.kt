package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.users.service.UserService
import org.springframework.stereotype.Component

@Component
class UserAuthrizeMiddleware(private val userService: UserService) : BaseMiddleware({ req, context ->
    val token = req.headers().firstHeader("x-access-token") ?: throw AuthException
    val currentUser = userService.getUserByCredentialHash(token)
    context.copy(user = currentUser)
})
