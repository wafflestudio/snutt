package com.wafflestudio.snutt.timetablelecturereminder.service

import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.util.SemesterUtils
import com.wafflestudio.snutt.notification.service.PushService
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableAndReminder
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
import java.time.temporal.ChronoUnit

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
        val timetablesById =
            timetableRepository.findAllById(chunkedReminders.map { it.timetableId }).toList().associateBy { it.id }
        val timetableAndReminders =
            chunkedReminders.mapNotNull { reminder ->
                timetablesById[reminder.timetableId]?.let { timetable ->
                    TimetableAndReminder(timetable, reminder)
                }
            }

        val nextSemesterItems =
            timetableAndReminders.filter {
                it.timetable.isAfterSemester(currentYear, currentSemester)
            }
        val pastSemesterItems =
            timetableAndReminders.filter {
                it.timetable.isBeforeSemester(currentYear, currentSemester)
            }
        val (currentSemesterPrimaryTableItems, currentSemesterNonPrimaryTableItems) =
            timetableAndReminders
                .filter {
                    it.timetable.isInSemester(currentYear, currentSemester)
                }.partition { it.timetable.isPrimary == true }

        if (currentSemesterPrimaryTableItems.isNotEmpty()) {
            sendPushes(currentSemesterPrimaryTableItems)
            markRemindersAsNotified(currentSemesterPrimaryTableItems.map { it.reminder }, now)
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
            deletePastSemesterReminders(pastSemesterItems.map { it.reminder })
            logger.info("${pastSemesterItems.size}개의 지난 학기 리마인더를 삭제했습니다.")
        }
    }

    private suspend fun getTargetReminders(now: Instant): List<TimetableLectureReminder> {
        val scheduleTo = TimetableLectureReminder.Schedule.fromInstant(now)
        val scheduleFrom = scheduleTo.minusMinutes(REMINDER_TIME_WINDOW_MINUTES.toInt())
        val lastNotifiedBefore = now.minus(REMINDER_TIME_WINDOW_MINUTES, ChronoUnit.MINUTES)

        val reminders =
            if (scheduleFrom.day == scheduleTo.day) {
                // 같은 날짜 내에서의 스케줄
                timetableLectureReminderRepository.findDueRemindersInTimeRange(
                    dayOfWeek = scheduleTo.day.value,
                    startMinute = scheduleFrom.minute,
                    endMinute = scheduleTo.minute,
                    lastNotifiedBefore = lastNotifiedBefore,
                )
            } else {
                // 다른 날짜(자정 즈음)
                timetableLectureReminderRepository.findDueRemindersInTimeRange( // 어제 스케줄
                    dayOfWeek = scheduleFrom.day.value,
                    startMinute = scheduleFrom.minute,
                    endMinute = 1439, // 23:59
                    lastNotifiedBefore = lastNotifiedBefore,
                ) +
                    timetableLectureReminderRepository.findDueRemindersInTimeRange( // 오늘 스케줄
                        dayOfWeek = scheduleTo.day.value,
                        startMinute = 0,
                        endMinute = scheduleTo.minute,
                        lastNotifiedBefore = lastNotifiedBefore,
                    )
            }

        return reminders
    }

    private suspend fun sendPushes(targets: List<TimetableAndReminder>) {
        val userIdToPushMessage =
            targets
                .mapNotNull {
                    val pushMessage = it.toPushMessage() ?: return@mapNotNull null
                    it.timetable.userId to pushMessage
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

    private suspend fun deletePastSemesterReminders(reminders: List<TimetableLectureReminder>) {
        if (reminders.isEmpty()) return
        timetableLectureReminderRepository.deleteAll(reminders)
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

    private fun Timetable.isAfterSemester(
        year: Int,
        semester: Semester,
    ) = this.year > year || (this.year == year && this.semester > semester)

    private fun Timetable.isInSemester(
        year: Int,
        semester: Semester,
    ) = this.year == year && this.semester == semester

    private fun Timetable.isBeforeSemester(
        year: Int,
        semester: Semester,
    ) = this.year < year || (this.year == year && this.semester < semester)

    private fun TimetableAndReminder.toPushMessage(): PushMessage? {
        val timetableLecture =
            timetable.lectures.find { it.id == reminder.timetableLectureId } ?: return null
        return PushMessage(
            timetableLecture.toPushTitle(),
            timetableLecture.toPushBody(reminder.offsetMinutes),
        )
    }

    private fun TimetableLecture.toPushTitle(): String =
        if (lectureId == null) {
            "\uD83D\uDCDA 일정 리마인더"
        } else {
            "\uD83D\uDCDA 강의 리마인더"
        }

    private fun TimetableLecture.toPushBody(offsetMinutes: Int): String {
        val typeString = if (lectureId == null) "일정" else "수업"
        return when {
            offsetMinutes == 0 -> "$courseTitle $typeString 시간이에요."
            offsetMinutes > 0 -> "$courseTitle $typeString 시작 ${offsetMinutes}분 후예요."
            else -> "$courseTitle $typeString 시작 ${-offsetMinutes}분 전이에요."
        }
    }
}
