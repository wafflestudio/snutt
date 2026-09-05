package com.wafflestudio.snutt.debug.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * [ApiDebugTarget] 에 등록된 유저의 API 호출 기록.
 *
 * 요청/응답 본문은 남기지 않는다. 목적이 "어떤 요청이 언제 왔고 어떻게 끝났는가"이고,
 * 전 API의 본문을 담으면 로그인 경로의 액세스 토큰·개인정보가 그대로 쌓이기 때문이다.
 * 특정 API의 본문이 필요해지면 그 엔드포인트만 따로 감싸는 편이 낫다.
 *
 * 임시 진단용이며, 원인 파악이 끝나면 이 커밋을 되돌린다.
 */
@Document("api_debug_logs")
data class ApiDebugLog(
    @Id
    val id: String? = null,
    @Indexed
    val userId: String,
    val nickname: String,
    val method: String,
    val path: String,
    /** 쿼리 스트링 원본. 없으면 null */
    val query: String?,
    /** 성공 시 응답 상태 코드. 예외로 끝난 경우 null */
    val status: Int?,
    val errorClass: String?,
    val errorMessage: String?,
    val osType: String,
    val appVersion: String?,
    val durationMs: Long,
    @Indexed(expireAfter = "14d")
    val createdAt: Instant = Instant.now(),
)
