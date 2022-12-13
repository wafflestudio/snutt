package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.common.exception.AppType
import com.wafflestudio.snu4t.common.exception.OsType
import com.wafflestudio.snu4t.handler.RequestContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class NativeClientInfoMiddleware() : BaseMiddleware({ req: ServerRequest, context: RequestContext ->
    val versionRegex = """^(\d+\.)?(\d+\.)?(\*|\d+)$""".toRegex()

    // TODO: 오류 정의해야 함
    context.apply {
        osType = OsType.fromString(req.headers().firstHeader("x-os-type"))
        osVersion = req.headers().firstHeader("x-os-version")
        appType = AppType.fromString(req.headers().firstHeader("x-app-type")) ?: AppType.RELEASE
        appVersion = req.headers().firstHeader("x-app-version")
    }
})
