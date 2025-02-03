package com.wafflestudio.snutt.users.data

import com.wafflestudio.snutt.users.repository.UserRepository
import com.wafflestudio.snutt.users.service.UserNicknameService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.bson.Document
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterConvertCallback
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserEntityCallback
    @Lazy
    constructor(
        private val userRepository: UserRepository,
        private val userNicknameService: UserNicknameService,
    ) : ReactiveAfterConvertCallback<User> {
        private val log = LoggerFactory.getLogger(javaClass)

        override fun onAfterConvert(
            entity: User,
            document: Document,
            collection: String,
        ): Publisher<User> {
            if (entity.nickname != null) {
                return Mono.just(entity)
            }

            log.info("empty nickname found. user: $entity")
            return backFillNickname(entity)
        }

        private fun backFillNickname(user: User): Mono<User> =
            mono(Dispatchers.Unconfined) {
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
