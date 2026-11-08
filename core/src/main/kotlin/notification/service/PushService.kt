package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.common.push.PushClient
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import org.springframework.stereotype.Service

/**
 * 유저 기기에 푸시를 보내는 서비스입니다.
 */
interface PushService {
    suspend fun sendPush(
        pushMessage: PushMessage,
        userId: String,
    )

    suspend fun sendPushes(
        pushMessage: PushMessage,
        userIds: List<String>,
    )

    suspend fun sendGlobalPush(pushMessage: PushMessage)

    suspend fun sendTargetPushes(userToPushMessage: Map<String, PushMessage>): TargetPushResult

    suspend fun sendPush(
        pushMessage: PushMessage,
        userId: String,
        pushPreferenceType: PushPreferenceType,
    )

    suspend fun sendPushes(
        pushMessage: PushMessage,
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType,
    )

    suspend fun sendTargetPushes(
        userToPushMessage: Map<String, PushMessage>,
        pushPreferenceType: PushPreferenceType,
    ): TargetPushResult
}

/**
 * @property userCount 발송 대상 유저 수 (푸시 설정 필터가 있으면 필터를 통과한 유저 수)
 * @property tokenCount 실제로 발송한 FCM 토큰 수
 */
data class TargetPushResult(
    val userCount: Int,
    val tokenCount: Int,
)

@Service
class PushServiceImpl internal constructor(
    private val deviceService: DeviceService,
    private val pushClient: PushClient,
    private val pushPreferenceService: PushPreferenceService,
) : PushService {
    override suspend fun sendPush(
        pushMessage: PushMessage,
        userId: String,
    ) {
        val userDevices = deviceService.getUserDevices(userId).ifEmpty { return }

        val targetedPushMessageWithTokens =
            userDevices.map { TargetedPushMessageWithToken(it.fcmRegistrationId, pushMessage) }
        pushClient.sendMessages(targetedPushMessageWithTokens)
    }

    override suspend fun sendPushes(
        pushMessage: PushMessage,
        userIds: List<String>,
    ) {
        val userIdToDevices = deviceService.getUsersDevices(userIds).ifEmpty { return }

        val targetedPushMessageWithTokens =
            userIdToDevices.values.flatMap { userDevices ->
                userDevices.map { TargetedPushMessageWithToken(it.fcmRegistrationId, pushMessage) }
            }

        pushClient.sendMessages(targetedPushMessageWithTokens)
    }

    override suspend fun sendGlobalPush(pushMessage: PushMessage) = pushClient.sendGlobalMessage(pushMessage)

    override suspend fun sendTargetPushes(userToPushMessage: Map<String, PushMessage>): TargetPushResult {
        val usersDevicesMap = deviceService.getUsersDevices(userToPushMessage.keys.toList())
        val targetedPushMessageWithTokens =
            userToPushMessage.entries
                .flatMap { (userId, pushMessage) ->
                    usersDevicesMap[userId]?.map { it.fcmRegistrationId to pushMessage } ?: listOf()
                }.map { (fcmRegistrationId, message) -> TargetedPushMessageWithToken(fcmRegistrationId, message) }
        pushClient.sendMessages(targetedPushMessageWithTokens)
        return TargetPushResult(userCount = userToPushMessage.size, tokenCount = targetedPushMessageWithTokens.size)
    }

    override suspend fun sendPush(
        pushMessage: PushMessage,
        userId: String,
        pushPreferenceType: PushPreferenceType,
    ) {
        if (pushPreferenceService.isPushPreferenceEnabled(userId, pushPreferenceType)) {
            sendPush(pushMessage, userId)
        }
    }

    override suspend fun sendPushes(
        pushMessage: PushMessage,
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType,
    ) {
        val filteredUserIds = pushPreferenceService.filterUsersByPushPreference(userIds, pushPreferenceType)

        if (filteredUserIds.isNotEmpty()) {
            sendPushes(pushMessage, filteredUserIds)
        }
    }

    override suspend fun sendTargetPushes(
        userToPushMessage: Map<String, PushMessage>,
        pushPreferenceType: PushPreferenceType,
    ): TargetPushResult {
        val userPushPreferenceEnabled = pushPreferenceService.isPushPreferenceEnabled(userToPushMessage.keys.toList(), pushPreferenceType)

        val filteredUserToPushMessage =
            userToPushMessage.filterKeys { userId ->
                userPushPreferenceEnabled[userId] ?: pushPreferenceType.isEnabledByDefault
            }

        if (filteredUserToPushMessage.isEmpty()) {
            return TargetPushResult(userCount = 0, tokenCount = 0)
        }
        return sendTargetPushes(filteredUserToPushMessage)
    }
}
