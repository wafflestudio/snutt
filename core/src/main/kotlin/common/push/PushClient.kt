package com.wafflestudio.snu4t.common.push

import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.common.push.dto.PushTargetMessage
import com.wafflestudio.snu4t.common.push.dto.TopicMessage

internal interface PushClient {
    suspend fun sendMessage(pushMessage: PushTargetMessage)

    suspend fun sendMessages(pushMessages: List<PushTargetMessage>)

    suspend fun sendGlobalMessage(pushMessage: PushMessage)

    suspend fun sendTopicMessage(pushMessage: TopicMessage)

    suspend fun subscribeGlobalTopic(registrationId: String)

    suspend fun unsubscribeGlobalTopic(registrationId: String)
}
