package com.wafflestudio.snu4t.common.exception

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatusCode

class ProxyException(
    val statusCode: HttpStatusCode,
    val errorBody: Map<String, Any?>,
) : RuntimeException(ObjectMapper().writeValueAsString(errorBody))
