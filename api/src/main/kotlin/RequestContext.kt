package com.wafflestudio.snu4t

import com.wafflestudio.snu4t.users.data.User
import org.springframework.web.reactive.function.server.ServerRequest

data class RequestContext(
    var user: User? = null,
    var device: Device? = null,
) {

    companion object {
        val ATTRIBUTE_KEY = RequestContext::class.simpleName
    }

    data class Device(
        var osType: String,
        var osVersion: String,
        var appType: String,
        var appVersion: String,
    )
}

fun ServerRequest.context() = attributes().getOrPut(RequestContext.ATTRIBUTE_KEY) { RequestContext() } as RequestContext
