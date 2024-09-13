package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.common.client.AppType
import com.wafflestudio.snu4t.common.client.AppVersion
import com.wafflestudio.snu4t.common.client.ClientInfo
import com.wafflestudio.snu4t.common.client.OsType
import com.wafflestudio.snu4t.common.exception.InvalidAppTypeException
import com.wafflestudio.snu4t.common.exception.InvalidOsTypeException
import com.wafflestudio.snu4t.handler.RequestContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class NativeClientInfoMiddleware() : Middleware {
    override suspend fun invoke(
        req: ServerRequest,
        context: RequestContext,
    ): RequestContext =
        context.copy(
            clientInfo =
                ClientInfo(
                    osType =
                        req.headers().firstHeader("x-os-type")?.let {
                            OsType.from(it) ?: throw InvalidOsTypeException
                        } ?: OsType.UNKNOWN,
                    osVersion = req.headers().firstHeader("x-os-version"),
                    appType =
                        req.headers().firstHeader("x-app-type")?.let {
                            AppType.from(it) ?: throw InvalidAppTypeException
                        } ?: AppType.RELEASE,
                    appVersion = req.headers().firstHeader("x-app-version")?.let { AppVersion(it) },
                    deviceId = req.headers().firstHeader("x-device-id"),
                    deviceModel = req.headers().firstHeader("x-device-model"),
                ),
        )
}
