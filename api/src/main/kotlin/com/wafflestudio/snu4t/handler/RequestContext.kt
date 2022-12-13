package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.exception.AppType
import com.wafflestudio.snu4t.common.exception.OsType
import com.wafflestudio.snu4t.users.data.User
import org.springframework.web.reactive.function.server.ServerRequest

const val CONTEXT_ATTRIBUTE_KEY = "context"

data class RequestContext(
    var user: User? = null,
    var osType: OsType = OsType.UNKNOWN,
    var osVersion: String? = null,
    var appType: AppType? = null,
    var appVersion: String? = null,
)

fun ServerRequest.getContext(): RequestContext {
    return this.attributes().getOrPut(CONTEXT_ATTRIBUTE_KEY) { RequestContext() } as RequestContext
}

fun ServerRequest.setContext(value: RequestContext) {
    this.attributes()[CONTEXT_ATTRIBUTE_KEY] = value
}
