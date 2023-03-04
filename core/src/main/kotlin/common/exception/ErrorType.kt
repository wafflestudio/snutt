package com.wafflestudio.snu4t.common.exception

import org.springframework.http.HttpStatus

enum class ErrorType(
    val httpStatus: HttpStatus,
    val errorCode: Long,
    val errorMessage: String,
    val displayMessage: String = "현재 서비스 이용이 원활하지 않습니다. 이용에 불편을 드려 죄송합니다.",
) {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 0x0000, "API 호출에 실패하였습니다."),

    WRONG_API_KEY(HttpStatus.FORBIDDEN, 0x2000, "API 키가 잘못되었습니다."),
    NO_USER_TOKEN(HttpStatus.UNAUTHORIZED, 0x2001, "유저 토큰이 존재하지 않습니다."),
    WRONG_USER_TOKEN(HttpStatus.FORBIDDEN, 0x2002, "유저 토큰이 유효하지 않습니다", "로그인이 필요합니다."),

    INVALID_LOCAL_ID(HttpStatus.FORBIDDEN, 0x3000, "localId가 유효하지 않습니다.", "아이디는 4~32자의 영문자와 숫자로 이루어져야 합니다."),
    INVALID_PASSWORD(HttpStatus.FORBIDDEN, 0x3001, "password가 유효하지 않습니다.", "비밀번호는 6~20자로 영문자와 숫자를 모두 포함해야 합니다."),
    DUPLICATE_LOCAL_ID(HttpStatus.FORBIDDEN, 0x3002, "localId가 중복되었습니다.", "이미 사용중인 아이디입니다."),
    INVALID_EMAIL(HttpStatus.FORBIDDEN, 0x300F, "email이 유효하지 않습니다.", "이메일 형식이 올바르지 않습니다."),

    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, 0x4003, "lecture가 없습니다.", "해당 강의는 존재하지 않습니다."),

    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, 40000, "파라미터 누락"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 40001, "파라미터 값 오류"),
    INVALID_BODY_FIELD_VALUE(HttpStatus.BAD_REQUEST, 40002, "요청 바디 값 오류"),

    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, 40400, "timetable_id가 유효하지 않습니다", "존재하지 않는 시간표입니다."),
    SHARED_TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "shared_timetable_id가 유효하지 않습니다", "존재하지 않는 공유시간표입니다."),
}
