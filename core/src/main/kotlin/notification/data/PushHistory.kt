package com.wafflestudio.snutt.notification.data

import java.time.Instant

/**
 * 푸시 발송 1회(= push_id 1개)에 대한 기록. 유저 단위 정보는 담지 않는다.
 *
 * @property targetUserCount 푸시 설정 필터 전 대상 유저 수. topic(전체) 발송이면 null
 * @property sentUserCount 푸시 설정 필터와 기기 유무 필터를 통과해 실제로 발송한 유저 수. topic 발송이면 null
 * @property sentTokenCount 실제로 발송한 FCM 토큰 수. topic 발송이면 null
 */
data class PushHistory(
    val pushId: String,
    val pushType: String,
    val sentAt: Instant,
    val targetUserCount: Int?,
    val sentUserCount: Int?,
    val sentTokenCount: Int?,
    val isGlobal: Boolean,
    val topic: String?,
    val title: String,
)
