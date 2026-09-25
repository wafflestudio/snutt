package com.wafflestudio.snutt.debug.service

import com.wafflestudio.snutt.common.exception.SnuttException
import com.wafflestudio.snutt.common.util.CoroutineUtils
import com.wafflestudio.snutt.debug.data.ApiDebugLog
import com.wafflestudio.snutt.debug.repository.ApiDebugLogRepository
import com.wafflestudio.snutt.debug.repository.ApiDebugTargetRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 추적 대상 유저의 API 호출을 [ApiDebugLog] 로 남기는 임시 진단용 서비스.
 *
 * 대상 목록은 `api_debug_targets` 컬렉션에서 읽어 메모리에 캐시하며, 재배포 없이
 * mongosh 로 대상을 추가·제거할 수 있다. 원인 파악이 끝나면 이 커밋을 되돌린다.
 */
@Service
class ApiDebugService(
    private val apiDebugTargetRepository: ApiDebugTargetRepository,
    private val apiDebugLogRepository: ApiDebugLogRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var targetUserIds: Set<String> = emptySet()

    @Volatile
    private var refreshedAt: Long = 0

    private val refreshing = AtomicBoolean(false)

    /**
     * 기동 직후 첫 요청이 빈 캐시로 판별되는 것을 막기 위해 미리 한 번 읽어 둔다.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun warmUpTargets() {
        refreshTargetsIfStale()
    }

    /**
     * 추적 대상인지 판별한다. 매 요청마다 호출되므로 DB 를 조회하지 않고 캐시된 값만 본다.
     * 캐시가 오래되었으면 갱신을 예약만 하고 즉시 반환해, 요청 지연을 만들지 않는다.
     */
    fun isTarget(userId: String): Boolean {
        refreshTargetsIfStale()
        return userId in targetUserIds
    }

    private fun refreshTargetsIfStale() {
        val now = System.currentTimeMillis()
        if (now - refreshedAt < REFRESH_INTERVAL_MS) return
        if (!refreshing.compareAndSet(false, true)) return
        // 조회에 실패하더라도 다음 주기까지는 재시도하지 않는다.
        refreshedAt = now

        CoroutineUtils.applicationScope.launch {
            runCatching { apiDebugTargetRepository.findAll().map { it.userId }.toSet() }
                .onSuccess { targetUserIds = it }
                .onFailure { log.warn("API 추적 대상 목록 갱신 실패", it) }
            refreshing.set(false)
        }
    }

    /**
     * 호출 기록을 남긴다. 응답 경로를 막지 않도록 별도 스코프에서 처리하며,
     * 저장에 실패해도 예외를 밖으로 내보내지 않는다.
     */
    fun record(
        userId: String,
        nickname: String,
        method: String,
        path: String,
        query: String?,
        status: Int?,
        throwable: Throwable?,
        osType: String,
        appVersion: String?,
        durationMs: Long,
    ) {
        CoroutineUtils.applicationScope.launch {
            runCatching {
                apiDebugLogRepository.save(
                    ApiDebugLog(
                        userId = userId,
                        nickname = nickname,
                        method = method,
                        path = path,
                        query = query,
                        status = status ?: (throwable as? SnuttException)?.error?.httpStatus?.value(),
                        errorClass = throwable?.let { it::class.qualifiedName },
                        errorMessage = throwable?.message,
                        osType = osType,
                        appVersion = appVersion,
                        durationMs = durationMs,
                    ),
                )
            }.onFailure { log.warn("API 추적 로그 저장 실패 (userId: $userId)", it) }
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 60_000L
    }
}
