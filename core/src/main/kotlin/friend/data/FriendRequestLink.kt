package com.wafflestudio.snu4t.friend.data

import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.annotation.Collation
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Document
@Collation()
data class FriendRequestLink(
    @Id
    val id: String? = null,
    val fromUserId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Indexed(expireAfterSeconds = 0)
    val expireAt: LocalDateTime = LocalDateTime.now().plusDays(14),
    @Indexed
    val encodedString: String,
)

@Configuration
class RequestAutoExpireConfig(private val reactiveMongoTemplate: ReactiveMongoTemplate) {
    @PostConstruct
    fun setExpiration() {
        val indexOps = reactiveMongoTemplate.indexOps(FriendRequestLink::class.java)
        val indexDefinition = Index()
            .on("expireAt", Sort.Direction.ASC)
            .expire(1, TimeUnit.SECONDS)
        indexOps.ensureIndex(indexDefinition).subscribe()
    }
}
