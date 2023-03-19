package com.wafflestudio.snu4t.mock.notification

import com.wafflestudio.snu4t.common.push.PushNotificationService
import com.wafflestudio.snu4t.common.push.PushTargetMessage
import com.wafflestudio.snu4t.common.push.TopicMessage
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("test")
class MockPushNotificationService : PushNotificationService {
    override suspend fun sendMessage(pushMessage: PushTargetMessage) {
    }

    override suspend fun sendMessages(pushMessages: List<PushTargetMessage>) {
    }

    override suspend fun sendGlobalMessage(title: String, body: String) {
    }

    override suspend fun sendTopicMessage(pushMessage: TopicMessage) {
    }
}
