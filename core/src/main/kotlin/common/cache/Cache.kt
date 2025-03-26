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
        val redisValue = runCatching { redisTemplate.opsForValue().getAndAwait(builtKey.key) }.getOrNull()
        return if (redisValue != null) {
            objectMapper.readValue(redisValue, typeRef)
        } else {
            if (supplier == null) return null
            supplier().also { value ->
                coroutineScope.launch {
                    set(builtKey, value)
                }
            }
        }
    }

    override suspend fun <T : Any> set(
        builtKey: BuiltCacheKey,
        value: T?,
    ) {
        val redisValue = objectMapper.writeValueAsString(value)
        runCatching { redisTemplate.opsForValue().setAndAwait(builtKey.key, redisValue, builtKey.ttl) }
    }

    override suspend fun delete(builtKey: BuiltCacheKey) {
        runCatching { redisTemplate.deleteAndAwait(builtKey.key) }
    }

    override suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean =
        runCatching {
            redisTemplate.opsForValue().setIfAbsentAndAwait(builtKey.key, "true", builtKey.ttl)
        }.getOrDefault(false)

    override suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean =
        runCatching {
            redisTemplate.deleteAndAwait(builtKey.key) > 0
        }.getOrDefault(false)
}

suspend inline fun <reified T : Any> Cache.get(
    builtKey: BuiltCacheKey,
    noinline supplier: (suspend () -> T?)? = null,
): T? = this.get(builtKey, jacksonTypeRef(), supplier)
