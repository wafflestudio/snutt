package com.wafflestudio.snu4t.middleware

import com.wafflestudio.snu4t.common.client.AppType
import com.wafflestudio.snu4t.common.client.ClientInfo
import com.wafflestudio.snu4t.common.client.OsType
import org.springframework.stereotype.Component

@Component
class NativeClientInfoMiddleware() : BaseMiddleware({ req, context ->
    // TODO: 버전 검증 로직 필요
    // val versionRegex = """^(\d+\.)?(\d+\.)?(\*|\d+)$""".toRegex()

    // TODO: 오류 정의해야 함
    context.copy(
        clientInfo = ClientInfo(
            osType = OsType.fromString(req.headers().firstHeader("x-os-type")),
            osVersion = req.headers().firstHeader("x-os-version"),
            appType = AppType.fromString(req.headers().firstHeader("x-app-type")) ?: AppType.RELEASE,
            appVersion = req.headers().firstHeader("x-app-version"),
        )
    )
})
