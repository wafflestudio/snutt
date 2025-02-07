package com.wafflestudio.snutt.common.push

import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithTopic

interface PushClient {
    suspend fun sendMessage(pushMessage: TargetedPushMessageWithToken)

    suspend fun sendMessages(pushMessages: List<TargetedPushMessageWithToken>)

    suspend fun sendGlobalMessage(pushMessage: PushMessage)

    suspend fun sendTopicMessage(pushMessage: TargetedPushMessageWithTopic)

    suspend fun subscribeGlobalTopic(registrationId: String)

    suspend fun unsubscribeGlobalTopic(registrationId: String)
}
