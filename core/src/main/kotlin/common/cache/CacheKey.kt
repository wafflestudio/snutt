package com.wafflestudio.snu4t.common.cache

import java.time.Duration

interface BuiltCacheKey {
    val key: String
    val ttl: Duration
}

enum class CacheKey(
    private val keyFormat: String,
    private val ttl: Duration,
) {
    LOCK_REGISTER_LOCAL("lock:register_local:%s", Duration.ofMinutes(1)),
    ;

    fun build(vararg args: Any?): BuiltCacheKey {
        val key = keyFormat.format(*args)
        return object : BuiltCacheKey {
            override val key: String = key
            override val ttl: Duration = this@CacheKey.ttl
        }
    }
}
