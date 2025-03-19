package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.notification.data.PushPreference
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
        pushPreferenceRepository.save(
            pushPreferenceRepository.findByUserId(user.id!!)
                ?.copy(pushPreferences = pushPreferenceDto.pushPreferences)
                ?: PushPreference(
                    userId = user.id,
                    pushPreferences = pushPreferenceDto.pushPreferences,
                ),
        )
    }

    override suspend fun getPushPreferenceDto(user: User): PushPreferenceDto =
        pushPreferenceRepository.findByUserId(user.id!!)
            ?.let { PushPreferenceDto(it) }
            ?: PushPreferenceDto(
                pushPreferences = emptyList(),
            )

    override suspend fun isPushPreferenceEnabled(
        userId: String,
        pushPreferenceType: PushPreferenceType,
    ): Boolean {
        if (pushPreferenceType == PushPreferenceType.NORMAL) {
            return true
        }

        return pushPreferenceRepository
            .findByUserId(userId)
            ?.pushPreferences
            ?.any { it.type == pushPreferenceType && it.isEnabled }
            ?: false
    }

    override suspend fun filterUsersByPushPreference(
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType,
    ): List<String> {
        if (pushPreferenceType == PushPreferenceType.NORMAL) {
            return userIds
        }

        return pushPreferenceRepository
            .findByUserIdIn(userIds)
            .filter { pushPreference ->
                pushPreference.pushPreferences
                    .any { it.type == pushPreferenceType && it.isEnabled }
            }
            .map { it.userId }
    }
}
