package com.wafflestudio.snutt.mock.notification

import com.wafflestudio.snutt.notification.data.PushHistory
import com.wafflestudio.snutt.notification.service.PushHistoryRecorder
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class MockPushHistoryRecorder : PushHistoryRecorder {
    override fun record(pushHistory: PushHistory) {
    }
}
