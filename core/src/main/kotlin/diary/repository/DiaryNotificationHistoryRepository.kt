package com.wafflestudio.snutt.diary.repository

import com.wafflestudio.snutt.diary.data.DiaryNotificationHistory
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

interface DiaryNotificationHistoryRepository : CoroutineCrudRepository<DiaryNotificationHistory, String> {
    fun findAllByUserIdInAndRecentNotifiedAtBefore(
        userIds: Collection<String>,
        recentNotifiedAtBefore: Instant,
    ): Set<DiaryNotificationHistory>
}
