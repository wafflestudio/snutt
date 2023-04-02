package com.wafflestudio.snu4t.mock.notification

import com.wafflestudio.snu4t.common.push.PushNotificationService
import common.push.dto.MessagePayload
import common.push.dto.MessageReason
import common.push.dto.PushTargetMessage
import common.push.dto.TopicMessage
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("test")
class MockPushNotificationService : PushNotificationService {

    override suspend fun sendMessage(pushMessage: PushTargetMessage, reason: MessageReason) {
    }

    override suspend fun sendMessages(pushMessages: List<PushTargetMessage>, reason: MessageReason) {
    }

    override suspend fun sendGlobalMessage(payload: MessagePayload, reason: MessageReason) {
    }

    override suspend fun sendTopicMessage(pushMessage: TopicMessage, reason: MessageReason) {
    }
}
