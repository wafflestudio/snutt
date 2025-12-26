package com.wafflestudio.snutt.config

import com.wafflestudio.snutt.common.exception.WrongUserTokenException
import com.wafflestudio.snutt.users.data.User
import kotlinx.coroutines.reactor.mono
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

const val USER_ATTRIBUTE_KEY = "user"

@Component
class UserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.hasParameterAnnotation(CurrentUser::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> =
        mono {
            exchange.attributes[USER_ATTRIBUTE_KEY] as? User
                ?: throw WrongUserTokenException
        }
}
