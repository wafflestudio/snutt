package com.wafflestudio.snu4t.seatsnotification.repository

import com.wafflestudio.snu4t.seatsnotification.data.SeatNotification
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SeatNotificationRepository : CoroutineCrudRepository<SeatNotification, String>{
    fun findAllByLectureId(lectureId: String): Flow<SeatNotification>
}