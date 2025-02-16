package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.push.PushClient
import com.wafflestudio.snutt.notification.data.UserDevice
import com.wafflestudio.snutt.notification.repository.UserDeviceRepository
import com.wafflestudio.snutt.users.repository.UserRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DeviceService internal constructor(
    private val pushClient: PushClient,
    private val userDeviceRepository: UserDeviceRepository,
    private val userRepository: UserRepository,
    private val cache: Cache,
) {
    suspend fun addRegistrationId(
        userId: String,
        registrationId: String,
        clientInfo: ClientInfo,
    ) {
        val cacheKey = CacheKey.LOCK_ADD_FCM_REGISTRATION_ID.build(userId, registrationId)
        if (!cache.acquireLock(cacheKey)) return

        coroutineScope {
            launch {
                pushClient.subscribeGlobalTopic(registrationId)
            }

            launch {
                val userDevice =
                    clientInfo.deviceId?.let {
                        userDeviceRepository.findByUserIdAndDeviceIdAndIsDeletedFalse(userId, it)
                    } ?: userDeviceRepository.findByUserIdAndFcmRegistrationIdAndIsDeletedFalse(userId, registrationId)

                userDevice?.apply {
                    if (updateIfChanged(clientInfo, registrationId)) {
                        userDeviceRepository.save(this)
                    }
                } ?: run {
                    userDeviceRepository.save(
                        UserDevice(
                            userId = userId,
                            osType = clientInfo.osType,
                            osVersion = clientInfo.osVersion,
                            deviceId = clientInfo.deviceId,
                            deviceModel = clientInfo.deviceModel,
                            appType = clientInfo.appType,
                            appVersion = clientInfo.appVersion,
                            fcmRegistrationId = registrationId,
                        ),
                    )
                }
            }
        }

        cache.releaseLock(cacheKey)
    }

    private fun UserDevice.updateIfChanged(
        clientInfo: ClientInfo,
        registrationId: String,
    ): Boolean {
        var isUpdated = false

        fun <T> T.updateIfDifferent(newValue: T): T {
            if (this != newValue) {
                isUpdated = true
                return newValue
            }
            return this
        }

        fcmRegistrationId = fcmRegistrationId.updateIfDifferent(registrationId)
        osType = osType.updateIfDifferent(clientInfo.osType)
        osVersion = osVersion.updateIfDifferent(clientInfo.osVersion)
        deviceId = deviceId.updateIfDifferent(clientInfo.deviceId)
        deviceModel = deviceModel.updateIfDifferent(clientInfo.deviceModel)
        appType = appType.updateIfDifferent(clientInfo.appType)
        appVersion = appVersion.updateIfDifferent(clientInfo.appVersion)
        if (isUpdated) updatedAt = LocalDateTime.now()
        return isUpdated
    }

    suspend fun removeRegistrationId(
        userId: String,
        registrationId: String,
    ) = coroutineScope {
        launch {
            pushClient.unsubscribeGlobalTopic(registrationId)
        }

        launch {
            userDeviceRepository.findByUserIdAndFcmRegistrationIdAndIsDeletedFalse(userId, registrationId)?.let {
                it.isDeleted = true
                userDeviceRepository.save(it)
            }
        }
    }

    suspend fun getUserDevices(userId: String): List<UserDevice> {
        return userDeviceRepository.findByUserIdAndIsDeletedFalse(userId).distinctBy { it.fcmRegistrationId }
            .ifEmpty { // UserDevice 로 migration 되지 않은 경우를 고려
                userRepository.findByIdAndActiveTrue(userId)?.fcmKey?.let { fcmKey ->
                    // FCM API 의 한계로 기기 그룹 내의 registrationId 들을 알아낼 수는 없음
                    listOf(UserDevice.of(userId, fcmKey))
                } ?: emptyList()
            }
    }

    suspend fun getUsersDevices(userIds: List<String>): Map<String, List<UserDevice>> {
        val userDevices = userDeviceRepository.findByUserIdInAndIsDeletedFalse(userIds).distinctBy { it.fcmRegistrationId }

        // UserDevice 로 migration 되지 않은 경우를 고려
        val noDeviceUserIds = userIds.filter { userId -> userDevices.none { it.userId == userId } }
        val legacyUserDevices =
            userRepository.findByIdInAndActiveTrue(noDeviceUserIds).mapNotNull { user ->
                // FCM API 의 한계로 기기 그룹 내의 registrationId 들을 알아낼 수는 없음
                user.fcmKey?.let { fcmKey -> UserDevice.of(user.id!!, fcmKey) }
            }

        return (userDevices + legacyUserDevices).groupBy { it.userId }
    }
}
