package com.wafflestudio.snu4t.mock.notification

import com.wafflestudio.snu4t.common.push.PushClient
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snu4t.common.push.dto.TargetedPushMessageWithTopic
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("test")
internal class MockPushClient : PushClient {
    override suspend fun sendMessage(pushMessage: TargetedPushMessageWithToken) {
    }

    override suspend fun sendMessages(pushMessages: List<TargetedPushMessageWithToken>) {
    }

    override suspend fun sendGlobalMessage(pushMessage: PushMessage) {
    }

    override suspend fun sendTopicMessage(pushMessage: TargetedPushMessageWithTopic) {
    }

    override suspend fun subscribeGlobalTopic(registrationId: String) {
    }

    override suspend fun unsubscribeGlobalTopic(registrationId: String) {
    }
}
