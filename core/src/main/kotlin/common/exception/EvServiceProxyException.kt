package com.wafflestudio.snutt.common.exception

import org.springframework.http.HttpStatusCode

class EvServiceProxyException(
    val statusCode: HttpStatusCode,
    val errorBody: Map<String, Any?>,
) : RuntimeException()
