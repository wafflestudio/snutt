package com.wafflestudio.snu4t.filter

import com.wafflestudio.snu4t.RequestContext
import com.wafflestudio.snu4t.RequestContext.Device
import com.wafflestudio.snu4t.common.exception.AuthException
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
class DeviceFilter : HandlerFilterFunction<ServerResponse, ServerResponse> {

    override fun filter(
        request: ServerRequest,
        next: HandlerFunction<ServerResponse>,
    ): Mono<ServerResponse> = mono(Dispatchers.Unconfined) {
        val device = runCatching {
            Device(
                osType = request.headers().firstHeader("x-os-type").let(::requireNotNull),
                osVersion = request.headers().firstHeader("x-os-version").let(::requireNotNull),
                appType = request.headers().firstHeader("x-app-type").let(::requireNotNull),
                appVersion = request.headers().firstHeader("x-app-version").let(::requireNotNull),
            )
        }.getOrElse {
            throw AuthException
        }

        request.attributes()
            .compute(RequestContext.ATTRIBUTE_KEY) { _, oldValue ->
                if (oldValue == null) {
                    RequestContext(device = device)
                } else {
                    (oldValue as RequestContext).copy(device = device)
                }
            }

        next.handle(request).awaitSingle()
    }
}
