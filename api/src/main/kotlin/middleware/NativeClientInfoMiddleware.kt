package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.common.client.AppType
import com.wafflestudio.snu4t.common.client.ClientInfo
import com.wafflestudio.snu4t.common.client.OsType
import com.wafflestudio.snu4t.handler.RequestContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class NativeClientInfoMiddleware() : Middleware {
    override suspend fun invoke(req: ServerRequest, context: RequestContext): RequestContext =
        context.copy(
            clientInfo = ClientInfo(
                osType = OsType.from(req.headers().firstHeader("x-os-type")),
                osVersion = req.headers().firstHeader("x-os-version"),
                appType = AppType.from(req.headers().firstHeader("x-app-type")) ?: AppType.RELEASE,
                appVersion = req.headers().firstHeader("x-app-version"),
                deviceId = req.headers().firstHeader("x-device-id"),
                deviceModel = req.headers().firstHeader("x-device-model"),
            )
        )
}
