package com.wafflestudio.snutt.common.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
import org.springframework.data.redis.core.setIfAbsentAndAwait
import org.springframework.stereotype.Component

interface Cache {
    suspend fun <T : Any> Cache.get(
        builtKey: BuiltCacheKey,
        typeRef: TypeReference<T>,
        supplier: (suspend () -> T?)? = null,
    ): T?

    suspend fun <T : Any> set(
        builtKey: BuiltCacheKey,
        value: T?,
    )

    suspend fun delete(builtKey: BuiltCacheKey)

    suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean

    suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean
}

@Component
class RedisCache(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : Cache {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    override suspend fun <T : Any> Cache.get(
        builtKey: BuiltCacheKey,
        typeRef: TypeReference<T>,
        supplier: (suspend () -> T?)?,
    ): T? {
        try {
            log.debug("[CACHE GET] {}", builtKey.key)
            val redisValue = redisTemplate.opsForValue().getAndAwait(builtKey.key)
            redisValue?.let {
                return objectMapper.readValue(it, typeRef)
            }
        } catch (e: Exception) {
            log.error(e.message, e)
        }

        if (supplier == null) return null

        val value = supplier()

        coroutineScope.launch { set(builtKey, value) }

        return value
    }

    override suspend fun <T : Any> set(
        builtKey: BuiltCacheKey,
        value: T?,
    ) {
        try {
            val redisValue = objectMapper.writeValueAsString(value)

            log.debug("[CACHE SET] {}", builtKey.key)
            redisTemplate.opsForValue().setAndAwait(builtKey.key, redisValue, builtKey.ttl)
        } catch (_: Exception) {
        }
    }

    override suspend fun delete(builtKey: BuiltCacheKey) {
        try {
            log.debug("[CACHE DEL] {}", builtKey.key)
            redisTemplate.deleteAndAwait(builtKey.key)
        } catch (_: Exception) {
        }
    }

    override suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean {
        log.debug("[CACHE SETNX] {}", builtKey.key)
        return redisTemplate.opsForValue().setIfAbsentAndAwait(builtKey.key, "true", builtKey.ttl)
    }

    override suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean {
        log.debug("[CACHE DEL] {}", builtKey.key)
        return redisTemplate.deleteAndAwait(builtKey.key) > 0
    }
}

suspend inline fun <reified T : Any> Cache.get(
    builtKey: BuiltCacheKey,
    noinline supplier: (suspend () -> T?)? = null,
): T? = this.get(builtKey, jacksonTypeRef(), supplier)
