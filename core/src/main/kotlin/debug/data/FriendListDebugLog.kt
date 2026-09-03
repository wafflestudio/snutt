package com.wafflestudio.snutt.debug.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * 친구 목록 조회 API(GET /v1/friends)의 요청/응답을 그대로 남겨 두기 위한 임시 진단용 컬렉션.
 *
 * 파드가 자주 재시작되어 애플리케이션 로그가 유실되는 상황이라 DB 에 적재한다.
 * userId 는 조회 편의를 위해 ObjectId 가 아닌 문자열로 저장한다.
 * 원인 파악이 끝나면 이 커밋을 되돌려 debug 패키지와 호출부를 함께 제거한다.
 */
@Document("friend_list_debug_logs")
data class FriendListDebugLog(
    @Id
    val id: String? = null,
    @Indexed
    val requesterUserId: String,
    val requesterNickname: String,
    /** state 요청 파라미터 원본. FriendState 로 파싱되지 않으면 500 이 난다. */
    val state: String,
    val osType: String,
    val appVersion: String?,
    val success: Boolean,
    /** 성공 시 응답 본문 JSON 전문. 직렬화에 실패하면 null */
    val responseJson: String?,
    val friendCount: Int?,
    val errorClass: String?,
    val errorMessage: String?,
    /** 예상 못 한 예외의 발생 지점 확인용. 4000자에서 자른다. */
    val stackTrace: String?,
    @Indexed(expireAfter = "14d")
    val createdAt: Instant = Instant.now(),
)
