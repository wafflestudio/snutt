package com.wafflestudio.snutt.common.exception

import com.wafflestudio.snutt.evaluation.dto.EvErrorResponse
import org.springframework.http.HttpStatusCode

class EvServiceProxyException(
    val statusCode: HttpStatusCode,
    val errorResponse: EvErrorResponse,
) : RuntimeException()
