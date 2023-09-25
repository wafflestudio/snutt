package com.wafflestudio.snu4t.common.push

import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snu4t.common.push.dto.TargetedPushMessageWithTopic

interface PushClient {
    suspend fun sendMessage(pushMessage: TargetedPushMessageWithToken)

    suspend fun sendMessages(pushMessages: List<TargetedPushMessageWithToken>)

    suspend fun sendGlobalMessage(pushMessage: PushMessage)

    suspend fun sendTopicMessage(pushMessage: TargetedPushMessageWithTopic)

    suspend fun subscribeGlobalTopic(registrationId: String)

    suspend fun unsubscribeGlobalTopic(registrationId: String)
}
