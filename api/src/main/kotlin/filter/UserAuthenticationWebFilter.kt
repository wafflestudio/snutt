package com.wafflestudio.snutt.filter

import com.wafflestudio.snutt.common.exception.NoUserTokenException
import com.wafflestudio.snutt.config.USER_ATTRIBUTE_KEY
import com.wafflestudio.snutt.users.service.UserService
import kotlinx.coroutines.reactor.mono
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * 사용자 인증 토큰을 검증하여 User 정보를 ServerWebExchange의 attributes에 저장하는 필터
 * ClientInfoWebFilter 이후에 실행되어야 하므로 Order를 높게 설정
 */
@Component
@Order(3)
class UserAuthenticationWebFilter(
    private val handlerAnnotationResolver: HandlerAnnotationResolver,
    private val userService: UserService,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> =
        handlerAnnotationResolver
            .isFilterTarget(exchange, UserAuthenticationWebFilterTarget::class.java)
            .flatMap { isTarget ->
                if (isTarget) {
                    val token = exchange.request.headers.getFirst("x-access-token") ?: throw NoUserTokenException
                    mono {
                        val user = userService.getUserByCredentialHash(token)
                        exchange.attributes[USER_ATTRIBUTE_KEY] = user
                    }.then(chain.filter(exchange))
                } else {
                    chain.filter(exchange)
                }
            }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UserAuthenticationWebFilterTarget
