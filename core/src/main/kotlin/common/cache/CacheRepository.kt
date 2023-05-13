package com.wafflestudio.snu4t.common.cache

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository

interface CacheRepository {
    suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean

    suspend fun flushDatabase()
}

@Repository
class RedisCacheRepository(
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate
) : CacheRepository {
    override suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean {
        return reactiveRedisTemplate.opsForValue().setIfAbsent(builtKey.key, "true", builtKey.ttl).awaitSingle()
    }

    override suspend fun flushDatabase() {
        reactiveRedisTemplate.execute { it.serverCommands().flushDb() }.awaitSingle()
    }
}
