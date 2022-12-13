package com.wafflestudio.snu4t.filter

import com.wafflestudio.snu4t.RequestContext
import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.users.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class AuthenticationFilter(
    private val userService: UserService,
) : HandlerFilterFunction<ServerResponse, ServerResponse> {

    override fun filter(
        request: ServerRequest,
        next: HandlerFunction<ServerResponse>,
    ): Mono<ServerResponse> = mono(Dispatchers.Unconfined) {
        val credentialHash = request.headers().firstHeader("x-access-token") ?: throw AuthException
        val user = userService.getUserByCredentialHash(credentialHash)

        request.attributes()
            .compute(RequestContext.ATTRIBUTE_KEY) { _, oldValue ->
                if (oldValue == null) {
                    RequestContext(user = user)
                } else {
                    (oldValue as RequestContext).copy(user = user)
                }
            }

        next.handle(request).awaitSingle()
    }
}
