package com.wafflestudio.snutt.common.cache

import java.time.Duration

interface BuiltCacheKey {
    val key: String
    val ttl: Duration
}

enum class CacheKey(
    private val keyFormat: String,
    private val ttl: Duration,
) {
    CLIENT_CONFIGS("client_configs:%s_%s", Duration.ofMinutes(5)), // osType, appVersion
    LOCK_REGISTER_LOCAL("lock:register_local:%s", Duration.ofMinutes(1)), // localId
    LOCK_ADD_FCM_REGISTRATION_ID("lock:add_registration_id:%s_%s", Duration.ofMinutes(1)), // userId, registrationId
    LOCK_SEND_TIMETABLE_LECTURE_REMINDER_NOTIFICATION("lock:send_timetable_lecture_reminder_notification", Duration.ofSeconds(55)),
    ;

    fun build(vararg args: Any?): BuiltCacheKey {
        val key = keyFormat.format(*args)
        return object : BuiltCacheKey {
            override val key: String = key
            override val ttl: Duration = this@CacheKey.ttl
        }
    }
}
