package com.wafflestudio.snutt.timetablelecturereminder.repository

import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.elemMatch
import org.springframework.data.mongodb.core.query.gte
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.lt
import org.springframework.data.mongodb.core.query.lte
import java.time.Instant

interface TimetableLectureReminderCustomRepository {
    suspend fun findDueRemindersInTimeRange(
        dayOfWeek: Int,
        startMinute: Int,
        endMinute: Int,
        lastNotifiedBefore: Instant,
    ): List<TimetableLectureReminder>
}

class TimetableLectureReminderCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableLectureReminderCustomRepository {
    override suspend fun findDueRemindersInTimeRange(
        dayOfWeek: Int,
        startMinute: Int,
        endMinute: Int,
        lastNotifiedBefore: Instant,
    ): List<TimetableLectureReminder> {
        val criteria =
            TimetableLectureReminder::schedules elemMatch
                Criteria().andOperator(
                    TimetableLectureReminder.Schedule::day isEqualTo dayOfWeek,
                    Criteria().andOperator(
                        TimetableLectureReminder.Schedule::minute gte startMinute,
                        TimetableLectureReminder.Schedule::minute lte endMinute,
                    ),
                    Criteria().orOperator(
                        TimetableLectureReminder.Schedule::recentNotifiedAt isEqualTo null,
                        TimetableLectureReminder.Schedule::recentNotifiedAt lt lastNotifiedBefore,
                    ),
                )

        return reactiveMongoTemplate
            .find<TimetableLectureReminder>(
                Query(criteria),
            ).collectList()
            .awaitSingle()
    }
}
