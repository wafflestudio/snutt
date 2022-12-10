package com.wafflestudio.snu4t.common.exception

import org.springframework.http.HttpStatus

enum class DefinedError(
        val httpStatus: HttpStatus,
        val errorCode: Long,
        val errorMessage: String,
        val displayMessage: String = "현재 서비스 이용이 원활하지 않습니다. 이용에 불편을 드려 죄송합니다."
) {
    // 0: 기본 에러
    DEFAULT_ERROR(HttpStatus.NOT_FOUND, 0, "API 호출에 실패하였습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 1000, "x-access-token 헤더값이 유효하지 않습니다", "로그인이 필요합니다."),
}
