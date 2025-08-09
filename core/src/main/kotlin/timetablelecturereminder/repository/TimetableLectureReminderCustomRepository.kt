package com.wafflestudio.snutt.timetablelecturereminder.repository

import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant
import java.time.temporal.ChronoUnit

interface TimetableLectureReminderCustomRepository {
    suspend fun findRemindersToSendNotifications(
        now: Instant,
        timeWindowMinutes: Long,
    ): List<TimetableLectureReminder>
}

class TimetableLectureReminderCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : TimetableLectureReminderCustomRepository {
    override suspend fun findRemindersToSendNotifications(
        now: Instant,
        timeWindowMinutes: Long,
    ): List<TimetableLectureReminder> {
        val scheduleTo = TimetableLectureReminder.Schedule.fromInstant(now)
        val scheduleFrom = scheduleTo.minusMinutes(timeWindowMinutes.toInt())

        val maxNotifiedAt = now.minus(timeWindowMinutes, ChronoUnit.MINUTES)

        val criteriaList = mutableListOf<Criteria>()

        if (scheduleTo.day == scheduleFrom.day) {
            // 같은 날짜
            criteriaList.add(
                Criteria.where("schedules").elemMatch(
                    Criteria
                        .where("day")
                        .`is`(scheduleTo.day)
                        .and("minute")
                        .gte(scheduleFrom.minute)
                        .lte(scheduleTo.minute)
                        .andOperator(
                            Criteria().orOperator(
                                Criteria.where("recentNotifiedAt").`is`(null),
                                Criteria.where("recentNotifiedAt").lt(maxNotifiedAt),
                            ),
                        ),
                ),
            )
        } else {
            // 다른 날짜(자정 즈음)
            // 어제 schedule
            criteriaList.add(
                Criteria.where("schedules").elemMatch(
                    Criteria
                        .where("day")
                        .`is`(scheduleFrom.day)
                        .and("minute")
                        .gte(scheduleFrom.minute)
                        .lte(1439) // 23:59
                        .andOperator(
                            Criteria().orOperator(
                                Criteria.where("recentNotifiedAt").`is`(null),
                                Criteria.where("recentNotifiedAt").lt(maxNotifiedAt),
                            ),
                        ),
                ),
            )
            // 오늘 schedule
            criteriaList.add(
                Criteria.where("schedules").elemMatch(
                    Criteria
                        .where("day")
                        .`is`(scheduleTo.day)
                        .and("minute")
                        .gte(0)
                        .lte(scheduleTo.minute)
                        .andOperator(
                            Criteria().orOperator(
                                Criteria.where("recentNotifiedAt").`is`(null),
                                Criteria.where("recentNotifiedAt").lt(maxNotifiedAt),
                            ),
                        ),
                ),
            )
        }

        val criteria = Criteria().orOperator(*criteriaList.toTypedArray())

        return reactiveMongoTemplate
            .find<TimetableLectureReminder>(
                Query(criteria),
            ).asFlow()
            .toList()
    }
}
