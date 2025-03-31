package com.wafflestudio.snutt.notification.repository

import com.wafflestudio.snutt.notification.data.PushPreference
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PushPreferenceRepository : CoroutineCrudRepository<PushPreference, String> {
    suspend fun findByUserId(userId: String): PushPreference?

    suspend fun findByUserIdIn(userIds: List<String>): List<PushPreference>
}
