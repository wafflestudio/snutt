package com.wafflestudio.snutt.timetablelecturereminder.repository

import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
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
            Criteria.where("schedules").elemMatch(
                Criteria
                    .where("day")
                    .`is`(dayOfWeek)
                    .and("minute")
                    .gte(startMinute)
                    .lte(endMinute)
                    .andOperator(
                        Criteria().orOperator(
                            Criteria.where("recentNotifiedAt").`is`(null),
                            Criteria.where("recentNotifiedAt").lt(lastNotifiedBefore),
                        ),
                    ),
            )

        return reactiveMongoTemplate
            .find<TimetableLectureReminder>(
                Query(criteria),
            ).asFlow()
            .toList()
    }
}
