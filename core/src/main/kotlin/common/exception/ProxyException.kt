package com.wafflestudio.snu4t.common.exception

import org.springframework.http.HttpStatusCode

class ProxyException(
    val statusCode: HttpStatusCode,
    val errorBody: Map<String, Any?>,
) : RuntimeException(errorBody.toString())
