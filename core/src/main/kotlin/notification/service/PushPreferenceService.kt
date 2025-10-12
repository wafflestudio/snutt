package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.notification.data.PushPreference
import com.wafflestudio.snutt.notification.data.PushPreferenceItem
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import com.wafflestudio.snutt.notification.dto.PushPreferenceDto
import com.wafflestudio.snutt.notification.repository.PushPreferenceRepository
import com.wafflestudio.snutt.users.data.User
import org.springframework.stereotype.Service

interface PushPreferenceService {
    suspend fun savePushPreference(
        user: User,
        pushPreferenceDto: PushPreferenceDto,
    )

    suspend fun getPushPreferenceDto(user: User): PushPreferenceDto

    suspend fun isPushPreferenceEnabled(
        userId: String,
        pushPreferenceType: PushPreferenceType,
    ): Boolean

    suspend fun isPushPreferenceEnabled(
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType
    ): Map<String, Boolean>

    suspend fun filterUsersByPushPreference(
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType,
    ): List<String>
}

@Service
class PushPreferenceServiceImpl(
    private val pushPreferenceRepository: PushPreferenceRepository,
) : PushPreferenceService {
    override suspend fun savePushPreference(
        user: User,
        pushPreferenceDto: PushPreferenceDto,
    ) {
        val pushPreferenceDtoMap = pushPreferenceDto.pushPreferences.associate { it.type to it.isEnabled }

        val pushPreferenceItemsWithDefault =
            PushPreferenceType.entries.map {
                PushPreferenceItem(
                    type = it,
                    isEnabled = pushPreferenceDtoMap[it] ?: it.isEnabledByDefault,
                )
            }

        pushPreferenceRepository.save(
            pushPreferenceRepository
                .findByUserId(user.id!!)
                ?.copy(pushPreferences = pushPreferenceItemsWithDefault)
                ?: PushPreference(
                    userId = user.id,
                    pushPreferences = pushPreferenceItemsWithDefault,
                ),
        )
    }

    override suspend fun getPushPreferenceDto(user: User): PushPreferenceDto =
        pushPreferenceRepository
            .findByUserId(user.id!!)
            ?.let { PushPreferenceDto(it) }
            ?: PushPreferenceDto(
                pushPreferences =
                    PushPreferenceType.entries
                        .filterNot { it == PushPreferenceType.NORMAL }
                        .map {
                            PushPreferenceItem(type = it, isEnabled = it.isEnabledByDefault)
                        },
            )

    override suspend fun isPushPreferenceEnabled(
        userId: String,
        pushPreferenceType: PushPreferenceType,
    ): Boolean = isPushPreferenceEnabled(listOf(userId), pushPreferenceType)[userId]!!

    override suspend fun isPushPreferenceEnabled(userIds: List<String>, pushPreferenceType: PushPreferenceType): Map<String, Boolean> {
        if (pushPreferenceType == PushPreferenceType.NORMAL) {
            return userIds.associateWith { true }
        }
        val userPushPreferences =  pushPreferenceRepository.findByUserIdIn(userIds).associateBy { it.userId }
        return userIds.associateWith { userId ->
            userPushPreferences[userId]?.pushPreferences?.find { it.type == pushPreferenceType }?.isEnabled ?: pushPreferenceType.isEnabledByDefault
        }
    }

    override suspend fun filterUsersByPushPreference(
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType,
    ): List<String> {
        if (pushPreferenceType == PushPreferenceType.NORMAL) {
            return userIds
        }

        val userIdsToCustomPushPreferences =
            pushPreferenceRepository
                .findByUserIdIn(userIds)
                .associateBy { it.userId }

        return userIds.filter { userId ->
            val customEnabled =
                userIdsToCustomPushPreferences[userId]
                    ?.pushPreferences
                    ?.find { it.type == pushPreferenceType }
                    ?.isEnabled

            customEnabled ?: pushPreferenceType.isEnabledByDefault
        }
    }
}
