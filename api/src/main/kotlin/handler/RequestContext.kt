package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.exception.WrongUserTokenException
import com.wafflestudio.snutt.users.data.User
import org.springframework.web.reactive.function.server.ServerRequest

const val CONTEXT_ATTRIBUTE_KEY = "context"

data class RequestContext(
    val user: User? = null,
    val clientInfo: ClientInfo? = null,
)

fun ServerRequest.getContext(): RequestContext {
    return this.attributes().getOrPut(CONTEXT_ATTRIBUTE_KEY) { RequestContext() } as RequestContext
}

fun ServerRequest.setContext(value: RequestContext) {
    this.attributes()[CONTEXT_ATTRIBUTE_KEY] = value
}

val ServerRequest.user: User
    get() = this.getContext().user ?: throw WrongUserTokenException

val ServerRequest.userId: String
    get() = this.getContext().user?.id ?: throw WrongUserTokenException

val ServerRequest.clientInfo: ClientInfo?
    get() = this.getContext().clientInfo
