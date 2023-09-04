package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserNicknameBackFillSupporter(
    private val userRepository: UserRepository,
    private val userNicknameService: UserNicknameService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = LoggerFactory.getLogger(javaClass)

    fun backFillNickname(user: User): Deferred<User> = scope.async {
        try {
            val result = userRepository.save(user.copy(nickname = userNicknameService.generateUniqueRandomNickname()))
            log.info("[BACKFILL] updated nickname of USER: $result")
            result
        } catch (e: Exception) {
            log.error("[BACKFILL] failed to update nickname of USER: $user", e)
            throw e
        }
    }
}
