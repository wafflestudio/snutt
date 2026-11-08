package com.wafflestudio.snutt.notification.service

import com.wafflestudio.snutt.common.push.PushClient
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snutt.notification.data.PushHistory
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 유저 기기에 푸시를 보내는 서비스입니다.
 *
 * 모든 발송 호출마다 push id를 하나 생성해 FCM data의 `push_id`, `push_type`으로 싣고,
 * 발송 결과를 [PushHistoryRecorder]로 기록합니다.
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
 * 푸시 발송 1회의 결과
 *
 * @property targetUserCount 푸시 설정 필터 전 대상 유저 수
 * @property sentUserCount 푸시 설정 필터와 기기 유무 필터를 통과해 실제로 발송한 유저 수
 * @property sentTokenCount 실제로 발송한 FCM 토큰 수
 */
data class TargetPushResult(
    val pushId: String,
    val pushType: String,
    val targetUserCount: Int,
    val sentUserCount: Int,
    val sentTokenCount: Int,
)

@Service
class PushServiceImpl internal constructor(
    private val deviceService: DeviceService,
    private val pushClient: PushClient,
    private val pushPreferenceService: PushPreferenceService,
    private val pushHistoryRecorder: PushHistoryRecorder,
) : PushService {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val PUSH_ID_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.of("Asia/Seoul"))
        private val PUSH_ID_SUFFIX_CHARS = ('a'..'z') + ('0'..'9')
        private const val PUSH_ID_SUFFIX_LENGTH = 6
        private const val GLOBAL_PUSH_TYPE = "global"
    }

    override suspend fun sendPush(
        pushMessage: PushMessage,
        userId: String,
    ) {
        sendToUsers(mapOf(userId to pushMessage), PushPreferenceType.NORMAL, targetUserCount = 1)
    }

    override suspend fun sendPushes(
        pushMessage: PushMessage,
        userIds: List<String>,
    ) {
        val distinctUserIds = userIds.distinct()
        sendToUsers(distinctUserIds.associateWith { pushMessage }, PushPreferenceType.NORMAL, distinctUserIds.size)
    }

    override suspend fun sendGlobalPush(pushMessage: PushMessage) {
        val sentAt = Instant.now()
        val pushType = GLOBAL_PUSH_TYPE
        val pushId = generatePushId(pushType, sentAt)
        pushClient.sendGlobalMessage(pushMessage.copy(pushId = pushId, pushType = pushType))
        log.info("푸시 전송: push_id={}, push_type={}, topic={}", pushId, pushType, PushClient.GLOBAL_TOPIC)
        pushHistoryRecorder.record(
            PushHistory(
                pushId = pushId,
                pushType = pushType,
                sentAt = sentAt,
                targetUserCount = null,
                sentUserCount = null,
                sentTokenCount = null,
                isGlobal = true,
                topic = PushClient.GLOBAL_TOPIC,
                title = pushMessage.title,
            ),
        )
    }

    override suspend fun sendTargetPushes(userToPushMessage: Map<String, PushMessage>): TargetPushResult =
        sendToUsers(userToPushMessage, PushPreferenceType.NORMAL, userToPushMessage.size)

    override suspend fun sendPush(
        pushMessage: PushMessage,
        userId: String,
        pushPreferenceType: PushPreferenceType,
    ) {
        val userToPushMessage =
            if (pushPreferenceService.isPushPreferenceEnabled(userId, pushPreferenceType)) {
                mapOf(userId to pushMessage)
            } else {
                emptyMap()
            }
        sendToUsers(userToPushMessage, pushPreferenceType, targetUserCount = 1)
    }

    override suspend fun sendPushes(
        pushMessage: PushMessage,
        userIds: List<String>,
        pushPreferenceType: PushPreferenceType,
    ) {
        val distinctUserIds = userIds.distinct()
        val filteredUserIds = pushPreferenceService.filterUsersByPushPreference(distinctUserIds, pushPreferenceType)
        sendToUsers(filteredUserIds.associateWith { pushMessage }, pushPreferenceType, distinctUserIds.size)
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

        return sendToUsers(filteredUserToPushMessage, pushPreferenceType, userToPushMessage.size)
    }

    /**
     * 토큰 단위 발송의 공통 경로. 호출 1회 = push id 1개.
     *
     * @param userToPushMessage 푸시 설정 필터를 이미 통과한 유저별 메시지
     * @param targetUserCount 푸시 설정 필터 전 대상 유저 수
     */
    private suspend fun sendToUsers(
        userToPushMessage: Map<String, PushMessage>,
        pushPreferenceType: PushPreferenceType,
        targetUserCount: Int,
    ): TargetPushResult {
        val sentAt = Instant.now()
        val pushType = pushPreferenceType.pushTypeValue
        val pushId = generatePushId(pushType, sentAt)

        val usersDevicesMap =
            if (userToPushMessage.isEmpty()) emptyMap() else deviceService.getUsersDevices(userToPushMessage.keys.toList())
        val sentUserIds = userToPushMessage.keys.filter { !usersDevicesMap[it].isNullOrEmpty() }
        val targetedPushMessageWithTokens =
            sentUserIds.flatMap { userId ->
                val pushMessage = userToPushMessage.getValue(userId).copy(pushId = pushId, pushType = pushType)
                usersDevicesMap.getValue(userId).map { TargetedPushMessageWithToken(it.fcmRegistrationId, pushMessage) }
            }
        if (targetedPushMessageWithTokens.isNotEmpty()) {
            pushClient.sendMessages(targetedPushMessageWithTokens)
        }

        val result =
            TargetPushResult(
                pushId = pushId,
                pushType = pushType,
                targetUserCount = targetUserCount,
                sentUserCount = sentUserIds.size,
                sentTokenCount = targetedPushMessageWithTokens.size,
            )
        log.info(
            "푸시 전송: push_id={}, push_type={}, target_user_count={}, sent_user_count={}, sent_token_count={}",
            result.pushId,
            result.pushType,
            result.targetUserCount,
            result.sentUserCount,
            result.sentTokenCount,
        )
        pushHistoryRecorder.record(
            PushHistory(
                pushId = pushId,
                pushType = pushType,
                sentAt = sentAt,
                targetUserCount = targetUserCount,
                sentUserCount = result.sentUserCount,
                sentTokenCount = result.sentTokenCount,
                isGlobal = false,
                topic = null,
                // 유저별로 제목이 다를 수 있어 대표값으로 첫 메시지 제목을 남긴다
                title =
                    userToPushMessage.values
                        .firstOrNull()
                        ?.title
                        .orEmpty(),
            ),
        )
        return result
    }

    private val PushPreferenceType.pushTypeValue: String
        get() = name.lowercase()

    /**
     * `{push_type}-{yyyyMMdd-HHmmss(Asia/Seoul)}-{6자리 랜덤}` 예: `diary-20261014-190000-k3x9ab`
     */
    private fun generatePushId(
        pushType: String,
        sentAt: Instant,
    ): String {
        val suffix = (1..PUSH_ID_SUFFIX_LENGTH).map { PUSH_ID_SUFFIX_CHARS.random() }.joinToString("")
        return "$pushType-${PUSH_ID_TIME_FORMATTER.format(sentAt)}-$suffix"
    }
}
