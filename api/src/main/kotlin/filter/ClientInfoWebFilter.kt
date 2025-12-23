package com.wafflestudio.snutt.filter

import com.wafflestudio.snutt.common.client.AppType
import com.wafflestudio.snutt.common.client.AppVersion
import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.client.OsType
import com.wafflestudio.snutt.common.exception.InvalidAppTypeException
import com.wafflestudio.snutt.common.exception.InvalidOsTypeException
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

const val CLIENT_INFO_ATTRIBUTE_KEY = "clientInfo"

/**
 * ClientInfo를 추출하여 ServerWebExchange의 attributes에 저장하는 필터
 * 모든 요청에 대해 실행되어야 하므로 Order를 낮게 설정
 */
@Component
@Order(2)
class ClientInfoWebFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val headers = exchange.request.headers
        val clientInfo =
            ClientInfo(
                osType =
                    headers.getFirst("x-os-type")?.let {
                        OsType.from(it) ?: throw InvalidOsTypeException
                    } ?: OsType.UNKNOWN,
                osVersion = headers.getFirst("x-os-version"),
                appType =
                    headers.getFirst("x-app-type")?.let {
                        AppType.from(it) ?: throw InvalidAppTypeException
                    } ?: AppType.RELEASE,
                appVersion = headers.getFirst("x-app-version")?.let { AppVersion(it) },
                deviceId = headers.getFirst("x-device-id"),
                deviceModel = headers.getFirst("x-device-model"),
            )
        exchange.attributes[CLIENT_INFO_ATTRIBUTE_KEY] = clientInfo

        return chain.filter(exchange)
    }
}
