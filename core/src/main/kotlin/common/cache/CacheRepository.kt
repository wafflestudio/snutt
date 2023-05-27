package com.wafflestudio.snu4t.common.cache

import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository

interface CacheRepository {
    suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean

    suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean

    suspend fun flushDatabase()
}

@Repository
class RedisCacheRepository(
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate
) : CacheRepository {
    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean {
        log.debug("[CACHE SETNX] {}", builtKey.key)
        return reactiveRedisTemplate.opsForValue().setIfAbsent(builtKey.key, "true", builtKey.ttl).awaitSingle()
    }

    override suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean {
        log.debug("[CACHE DEL] {}", builtKey.key)
        return reactiveRedisTemplate.delete(builtKey.key).awaitSingle() > 0
    }

    override suspend fun flushDatabase() {
        reactiveRedisTemplate.execute { it.serverCommands().flushDb() }.awaitSingle()
    }
}
