package com.wafflestudio.snutt.debug.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * API 호출 추적 대상 유저. 이 컬렉션에 문서를 넣으면 해당 유저의 모든 API 호출이
 * [ApiDebugLog] 로 쌓인다. 재배포 없이 mongosh 로 추가·제거할 수 있다.
 *
 * 예) db.api_debug_targets.insertOne({ userId: "제보자id", memo: "친구 목록 미표시 제보", createdAt: new Date() })
 *
 * 임시 진단용이며, 원인 파악이 끝나면 이 커밋을 되돌린다.
 */
@Document("api_debug_targets")
data class ApiDebugTarget(
    @Id
    val id: String? = null,
    @Indexed(unique = true)
    val userId: String,
    /** 왜 넣었는지 메모 */
    val memo: String? = null,
    val createdAt: Instant = Instant.now(),
)
