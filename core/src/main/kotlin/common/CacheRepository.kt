package com.wafflestudio.snu4t.common

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository

interface CacheRepository {
    suspend fun flushDatabase()
}

@Repository
class RedisCacheRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) : CacheRepository {
    override suspend fun flushDatabase() {
        reactiveRedisTemplate.execute { it.serverCommands().flushDb() }.awaitSingle()
    }
}
