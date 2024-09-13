package com.wafflestudio.snu4t.notification.repository

import com.wafflestudio.snu4t.notification.data.UserDevice
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserDeviceRepository : CoroutineCrudRepository<UserDevice, String> {
    suspend fun findByUserIdAndIsDeletedFalse(userId: String): List<UserDevice>

    suspend fun findByUserIdInAndIsDeletedFalse(userIds: List<String>): List<UserDevice>

    suspend fun findByUserIdAndDeviceIdAndIsDeletedFalse(
        userId: String,
        deviceId: String,
    ): UserDevice?

    suspend fun findByUserIdAndFcmRegistrationIdAndIsDeletedFalse(
        userId: String,
        registrationId: String,
    ): UserDevice?
}
