package com.wafflestudio.snu4t.users.data

import com.wafflestudio.snu4t.users.service.UserNicknameBackFillSupporter
import org.bson.Document
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.core.CoroutinesUtils
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterConvertCallback
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserEntityCallback(
    @Lazy private val supporter: UserNicknameBackFillSupporter
) : ReactiveAfterConvertCallback<User> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun onAfterConvert(entity: User, document: Document, collection: String): Publisher<User> {
        if (entity.nickname == null) {
            log.info("empty nickname found. user: $entity")
            return CoroutinesUtils.deferredToMono(supporter.backFillNickname(entity))
        }

        return Mono.just(entity)
    }
}
