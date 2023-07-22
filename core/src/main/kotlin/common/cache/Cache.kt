package com.wafflestudio.snu4t.common.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

interface Cache {
    suspend fun <T : Any> Cache.get(
        builtKey: BuiltCacheKey,
        typeRef: TypeReference<T>,
        supplier: (suspend () -> T?)? = null,
    ): T?

    suspend fun <T : Any> set(builtKey: BuiltCacheKey, value: T?)

    suspend fun delete(builtKey: BuiltCacheKey)

    suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean

    suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean

    suspend fun flushDatabase()
}

@Component
class RedisCache(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : Cache {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val coroutineScope = CoroutineScope(
            SupervisorJob() +
                Executors.newSingleThreadExecutor {
                    r ->
                    Thread(r, "snu4t-cache").apply { isDaemon = true }
                }.asCoroutineDispatcher()
        )
    }

    override suspend fun <T : Any> Cache.get(
        builtKey: BuiltCacheKey,
        typeRef: TypeReference<T>,
        supplier: (suspend () -> T?)?
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

    override suspend fun <T : Any> set(builtKey: BuiltCacheKey, value: T?) {
        runCatching {
            val redisValue = objectMapper.writeValueAsString(value)

            log.debug("[CACHE SET] {}", builtKey.key)
            redisTemplate.opsForValue().setAndAwait(builtKey.key, redisValue, builtKey.ttl)
        }.getOrElse {
            log.error(it.message, it)
        }
    }

    override suspend fun delete(builtKey: BuiltCacheKey) {
        runCatching {
            log.debug("[CACHE DELETE] {}", builtKey.key)
            redisTemplate.deleteAndAwait(builtKey.key)
        }.getOrElse {
            log.error(it.message, it)
        }
    }

    override suspend fun acquireLock(builtKey: BuiltCacheKey): Boolean {
        log.debug("[CACHE SETNX] {}", builtKey.key)
        return redisTemplate.opsForValue().setIfAbsent(builtKey.key, "true", builtKey.ttl).awaitSingle()
    }

    override suspend fun releaseLock(builtKey: BuiltCacheKey): Boolean {
        log.debug("[CACHE DEL] {}", builtKey.key)
        return redisTemplate.delete(builtKey.key).awaitSingle() > 0
    }

    override suspend fun flushDatabase() {
        redisTemplate.execute { it.serverCommands().flushDb() }.awaitSingle()
    }
}

suspend inline fun <reified T : Any> Cache.get(
    builtKey: BuiltCacheKey,
    noinline supplier: (suspend () -> T?)? = null,
): T? = this.get(builtKey, jacksonTypeRef(), supplier)
