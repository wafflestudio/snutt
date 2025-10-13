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

    suspend fun sendTargetPushes(userToPushMessage: Map<String, PushMessage>)

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
    )
}

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

    override suspend fun sendTargetPushes(userToPushMessage: Map<String, PushMessage>) {
        val usersDevicesMap = deviceService.getUsersDevices(userToPushMessage.keys.toList())
        userToPushMessage.entries
            .flatMap { (userId, pushMessage) ->
                usersDevicesMap[userId]?.map { it.fcmRegistrationId to pushMessage } ?: listOf()
            }.map { (fcmRegistrationId, message) -> TargetedPushMessageWithToken(fcmRegistrationId, message) }
            .let { pushClient.sendMessages(it) }
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
    ) {
        val userPushPreferenceEnabled = pushPreferenceService.isPushPreferenceEnabled(userToPushMessage.keys.toList(), pushPreferenceType)

        val filteredUserToPushMessage =
            userToPushMessage.filterKeys { userId ->
                userPushPreferenceEnabled[userId] ?: pushPreferenceType.isEnabledByDefault
            }

        if (filteredUserToPushMessage.isNotEmpty()) {
            sendTargetPushes(filteredUserToPushMessage)
        }
    }
}
