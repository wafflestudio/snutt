package com.wafflestudio.snutt.debug.service

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.debug.data.FriendListDebugLog
import com.wafflestudio.snutt.debug.repository.FriendListDebugLogRepository
import com.wafflestudio.snutt.friend.dto.FriendResponse
import com.wafflestudio.snutt.users.data.User
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

/**
 * 친구 목록 조회 API의 모든 호출을 [FriendListDebugLog] 로 남기는 임시 진단용 서비스.
 * 원인 파악이 끝나면 이 커밋을 되돌려 제거한다.
 */
@Service
class FriendListDebugService(
    private val friendListDebugLogRepository: FriendListDebugLogRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * [block] 의 요청/응답(또는 예외)을 DB 에 남긴다.
     * 기록 과정에서 발생하는 예외는 모두 삼켜 API 응답에 영향을 주지 않는다.
     */
    suspend fun capture(
        user: User,
        state: String,
        clientInfo: ClientInfo,
        block: suspend () -> ListResponse<FriendResponse>,
    ): ListResponse<FriendResponse> {
        val result =
            try {
                block()
            } catch (e: Throwable) {
                // 클라이언트 연결이 끊겨 코루틴이 취소된 경우에도 기록은 남긴다.
                withContext(NonCancellable) { record(user, state, clientInfo, null, e) }
                throw e
            }

        record(user, state, clientInfo, result, null)
        return result
    }

    private suspend fun record(
        user: User,
        state: String,
        clientInfo: ClientInfo,
        result: ListResponse<FriendResponse>?,
        throwable: Throwable?,
    ) {
        runCatching {
            friendListDebugLogRepository.save(
                FriendListDebugLog(
                    requesterUserId = user.id!!,
                    requesterNickname = user.nickname,
                    state = state,
                    osType = clientInfo.osType.name,
                    appVersion = clientInfo.appVersion?.appVersion,
                    success = throwable == null,
                    responseJson = result?.let(::toJsonOrNull),
                    friendCount = result?.totalCount,
                    errorClass = throwable?.let { it::class.qualifiedName },
                    errorMessage = throwable?.message,
                    stackTrace = throwable?.stackTraceToString()?.take(STACK_TRACE_MAX_LENGTH),
                ),
            )
        }.onFailure { log.warn("친구 목록 진단 로그 저장 실패 (userId: ${user.id})", it) }
    }

    private fun toJsonOrNull(value: Any): String? = runCatching { objectMapper.writeValueAsString(value) }.getOrNull()

    companion object {
        private const val STACK_TRACE_MAX_LENGTH = 4000
    }
}
