package com.wafflestudio.snutt.timetablelecturereminder.service

import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.util.SemesterUtils
import com.wafflestudio.snutt.notification.service.PushService
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import com.wafflestudio.snutt.timetablelecturereminder.repository.TimetableLectureReminderRepository
import com.wafflestudio.snutt.timetables.data.Timetable
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

interface TimetableLectureReminderNotifierService {
    suspend fun send()
}

@Service
class TimetableLectureReminderNotifierServiceImpl(
    private val cache: Cache,
    private val timetableLectureReminderRepository: TimetableLectureReminderRepository,
    private val timetableRepository: TimetableRepository,
    private val pushService: PushService,
) : TimetableLectureReminderNotifierService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val REMINDER_TIME_WINDOW_MINUTES = 10L
        private const val BATCH_SIZE = 500
    }

    @Scheduled(cron = "0 * * * * *", scheduler = "timetableLectureReminderTaskScheduler")
    override suspend fun send() {
        val lockKey = CacheKey.LOCK_SEND_TIMETABLE_LECTURE_REMINDER_NOTIFICATION.build()
        cache.withLock(lockKey) {
            try {
                logger.debug("강의 리마인더 알림 전송 작업을 시작합니다.")
                val now = Instant.now()
                val (currentYear, currentSemester) =
                    SemesterUtils.getCurrentYearAndSemester(now) ?: run {
                        logger.debug("현재 진행 중인 학기가 없습니다.")
                        return@withLock
                    }
                val reminders = getTargetReminders(now)

                if (reminders.isEmpty()) {
                    logger.debug("현재 시간대에 전송할 강의 리마인더가 없습니다.")
                    return@withLock
                }

                logger.info("총 ${reminders.size}개의 강의 리마인더를 찾았습니다.")
                processReminders(reminders, currentYear, currentSemester, now)
            } catch (e: Exception) {
                logger.error("강의 리마인더 알림 전송 중 오류 발생", e)
            }
        }
    }

    private suspend fun processReminders(
        reminders: List<TimetableLectureReminder>,
        currentYear: Int,
        currentSemester: Semester,
        now: Instant,
    ) {
        reminders.chunked(BATCH_SIZE).forEachIndexed { index, chunkedReminders ->
            logger.debug("배치 ${index + 1} 처리 중: ${chunkedReminders.size}개의 리마인더")
            processChunkedReminders(chunkedReminders, currentYear, currentSemester, now)
        }
    }

    private suspend fun processChunkedReminders(
        chunkedReminders: List<TimetableLectureReminder>,
        currentYear: Int,
        currentSemester: Semester,
        now: Instant,
    ) {
        val timetables = timetableRepository.findAllById(chunkedReminders.map { it.timetableId }).toList()
        val timetableToReminderMap = mapTimetablesToReminders(timetables, chunkedReminders)

        val nextSemesterItems = mutableListOf<Pair<Timetable, TimetableLectureReminder>>()
        val pastSemesterItems = mutableListOf<Pair<Timetable, TimetableLectureReminder>>()
        val currentSemesterPrimaryTableItems = mutableListOf<Pair<Timetable, TimetableLectureReminder>>()
        val currentSemesterNonPrimaryTableItems = mutableListOf<Pair<Timetable, TimetableLectureReminder>>()

        timetableToReminderMap.forEach { pair ->
            val (timetable, _) = pair
            when {
                timetable.year > currentYear ||
                    (timetable.year == currentYear && timetable.semester > currentSemester) -> {
                    nextSemesterItems.add(pair)
                }
                timetable.year < currentYear ||
                    (timetable.year == currentYear && timetable.semester < currentSemester) -> {
                    pastSemesterItems.add(pair)
                }
                else -> {
                    if (timetable.isPrimary == true) {
                        currentSemesterPrimaryTableItems.add(pair)
                    } else {
                        currentSemesterNonPrimaryTableItems.add(pair)
                    }
                }
            }
        }

        if (currentSemesterPrimaryTableItems.isNotEmpty()) {
            sendPushes(currentSemesterPrimaryTableItems)
            markRemindersAsNotified(currentSemesterPrimaryTableItems.map { it.second }, now)
            logger.info("${currentSemesterPrimaryTableItems.size}개의 현재 학기 리마인더에 알림을 보냈습니다.")
        }

        if (currentSemesterNonPrimaryTableItems.isNotEmpty()) {
            logger.debug("${currentSemesterNonPrimaryTableItems.size}개의 대표시간표의 강의가 아닌 현재 학기 리마인더를 건너뛰었습니다.")
        }

        if (nextSemesterItems.isNotEmpty()) {
            logger.debug("${nextSemesterItems.size}개의 다음 학기 리마인더를 건너뛰었습니다.")
        }

        if (pastSemesterItems.isNotEmpty()) {
            // 앞으로 알림 보낼 일 없는 리마인더이므로 삭제한다.
            deletePastSemesterReminders(pastSemesterItems.map { it.second })
            logger.info("${pastSemesterItems.size}개의 지난 학기 리마인더를 삭제했습니다.")
        }
    }

    private fun mapTimetablesToReminders(
        timetables: List<Timetable>,
        reminders: List<TimetableLectureReminder>,
    ): List<Pair<Timetable, TimetableLectureReminder>> {
        val reminderByTimetableId = reminders.associateBy { it.timetableId }
        return timetables.mapNotNull { timetable ->
            val reminder = reminderByTimetableId[timetable.id] ?: return@mapNotNull null
            timetable to reminder
        }
    }

    private suspend fun getTargetReminders(now: Instant): List<TimetableLectureReminder> =
        timetableLectureReminderRepository.findRemindersToSendNotifications(now, REMINDER_TIME_WINDOW_MINUTES)

    private suspend fun sendPushes(targets: List<Pair<Timetable, TimetableLectureReminder>>) {
        val userIdToPushMessage =
            targets
                .mapNotNull { (timetable, reminder) ->
                    val timetableLecture =
                        timetable.lectures.find { it.id == reminder.timetableLectureId } ?: return@mapNotNull null
                    val pushMessage =
                        PushMessage(
                            getPushTitle(timetableLecture),
                            getPushBody(timetableLecture, reminder.offsetMinutes),
                        )
                    timetable.userId to pushMessage
                }.toMap()

        if (userIdToPushMessage.isNotEmpty()) {
            pushService.sendTargetPushes(userIdToPushMessage)
        }
    }

    private suspend fun markRemindersAsNotified(
        reminders: List<TimetableLectureReminder>,
        now: Instant,
    ) {
        if (reminders.isEmpty()) return

        val scheduleTo = TimetableLectureReminder.Schedule.fromInstant(now)
        val scheduleFrom = scheduleTo.minusMinutes(REMINDER_TIME_WINDOW_MINUTES.toInt())

        val updatedReminders =
            reminders.map { reminder ->
                val newSchedules =
                    reminder.schedules.map { schedule ->
                        // 강의 하나의 schedule이 10분 윈도우 안에 여러 개라면
                        // 알림을 1분마다 하나씩 보낼 필요 없이 한 번만 보내고
                        // schedule은 전부 recentNotifiedAt을 기록하여 리소스를 아낀다.
                        if (schedule.shouldBeMarkedAsNotified(scheduleFrom, scheduleTo, now)) {
                            schedule.copy(recentNotifiedAt = now)
                        } else {
                            schedule
                        }
                    }
                reminder.schedules = newSchedules
                reminder
            }

        timetableLectureReminderRepository.saveAll(updatedReminders).collect()
    }

    private fun TimetableLectureReminder.Schedule.shouldBeMarkedAsNotified(
        scheduleFrom: TimetableLectureReminder.Schedule,
        scheduleTo: TimetableLectureReminder.Schedule,
        now: Instant,
    ): Boolean {
        val isInTimeWindow = this.isWithin(scheduleFrom, scheduleTo)
        val hasNotBeenNotifiedRecently =
            this.recentNotifiedAt == null ||
                this.recentNotifiedAt < now.minusSeconds(REMINDER_TIME_WINDOW_MINUTES * 60L)

        return isInTimeWindow && hasNotBeenNotifiedRecently
    }

    private suspend fun deletePastSemesterReminders(reminders: List<TimetableLectureReminder>) {
        if (reminders.isEmpty()) return
        timetableLectureReminderRepository.deleteAll(reminders)
    }

    private fun getPushTitle(timetableLecture: TimetableLecture): String =
        if (timetableLecture.lectureId == null) {
            "\uD83D\uDCDA 일정 리마인더"
        } else {
            "\uD83D\uDCDA 강의 리마인더"
        }

    private fun getPushBody(
        timetableLecture: TimetableLecture,
        offsetMinutes: Int,
    ): String {
        val typeString = if (timetableLecture.lectureId == null) "일정" else "수업"
        return if (offsetMinutes == 0) {
            "${timetableLecture.courseTitle} $typeString 시간이에요."
        } else if (offsetMinutes > 0) {
            "${timetableLecture.courseTitle} $typeString 시작 ${offsetMinutes}분 후예요."
        } else {
            "${timetableLecture.courseTitle} $typeString 시작 ${offsetMinutes}분 전이에요."
        }
    }
}
