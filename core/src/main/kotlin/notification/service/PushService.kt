package com.wafflestudio.snu4t.notification.service

import com.wafflestudio.snu4t.common.push.dto.PushMessage

/**
 * 유저 기기에 푸시를 보내는 서비스입니다.
 */
interface PushService {
    /**
     * [com.wafflestudio.snu4t.notification.data.Notification]을 저장하지 않고 푸시만 보내므로 사용 시 주의가 필요합니다.
     *
     * @see [PushNotificationService.sendPushAndNotification]
     */
    suspend fun sendPush(pushMessage: PushMessage, userId: String)

    /**
     * [com.wafflestudio.snu4t.notification.data.Notification]을 저장하지 않고 푸시만 보내므로 사용 시 주의가 필요합니다.
     *
     * @see [PushNotificationService.sendPushesAndNotifications]
     */
    suspend fun sendPushes(pushMessage: PushMessage, userIds: List<String>)
}
