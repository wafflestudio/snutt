package com.wafflestudio.snutt.mock.notification

import com.wafflestudio.snutt.common.push.PushClient
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithTopic
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class MockPushClient : PushClient {
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
