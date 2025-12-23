package com.wafflestudio.snutt.filter

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
 *
 * 토큰이 없거나 유효하지 않아도 예외를 발생시키지 않고 계속 진행합니다.
 * 실제 인증 여부는 Controller에서 @CurrentUser나 @CurrentUserId를 사용할 때 확인됩니다.
 */
@Component
@Order(3)
class UserAuthenticationWebFilter(
    private val userService: UserService,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val token = exchange.request.headers.getFirst("x-access-token")

        return if (token != null) {
            mono {
                try {
                    val user = userService.getUserByCredentialHash(token)
                    exchange.attributes[USER_ATTRIBUTE_KEY] = user
                } catch (_: Exception) {
                    // 토큰이 유효하지 않아도 계속 진행 (Controller에서 @CurrentUser 사용 시 예외 발생)
                }
            }.then(chain.filter(exchange))
        } else {
            chain.filter(exchange)
        }
    }
}
