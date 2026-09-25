package com.wafflestudio.snutt.filter

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.client.OsType
import com.wafflestudio.snutt.config.USER_ATTRIBUTE_KEY
import com.wafflestudio.snutt.debug.service.ApiDebugService
import com.wafflestudio.snutt.users.data.User
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * 추적 대상 유저의 API 호출을 기록하는 임시 진단용 필터.
 *
 * [UserAuthenticationWebFilter] 가 채워 둔 유저 정보를 읽어야 하므로 그보다 안쪽(Order 5)에 둔다.
 * 인증을 타지 않는 엔드포인트는 유저가 없어 기록되지 않으며, 그 덕분에 로그인 경로는 대상에서 빠진다.
 *
 * 기록은 응답 경로를 막지 않는다. [ApiDebugService.record] 가 별도 스코프에서 처리한다.
 * 원인 파악이 끝나면 이 커밋을 되돌린다.
 */
@Component
@Order(5)
class ApiDebugWebFilter(
    private val apiDebugService: ApiDebugService,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val startedAt = System.currentTimeMillis()

        return chain
            .filter(exchange)
            .doOnSuccess { record(exchange, null, startedAt) }
            .doOnError { record(exchange, it, startedAt) }
    }

    private fun record(
        exchange: ServerWebExchange,
        throwable: Throwable?,
        startedAt: Long,
    ) {
        val user = exchange.attributes[USER_ATTRIBUTE_KEY] as? User ?: return
        val userId = user.id ?: return
        if (!apiDebugService.isTarget(userId)) return

        val clientInfo = exchange.attributes[CLIENT_INFO_ATTRIBUTE_KEY] as? ClientInfo

        apiDebugService.record(
            userId = userId,
            nickname = user.nickname,
            method = exchange.request.method.name(),
            path = exchange.request.path.value(),
            query = exchange.request.uri.rawQuery,
            status =
                exchange.response.statusCode
                    ?.value()
                    .takeIf { throwable == null },
            throwable = throwable,
            osType = clientInfo?.osType?.name ?: OsType.UNKNOWN.name,
            appVersion = clientInfo?.appVersion?.appVersion,
            durationMs = System.currentTimeMillis() - startedAt,
        )
    }
}
