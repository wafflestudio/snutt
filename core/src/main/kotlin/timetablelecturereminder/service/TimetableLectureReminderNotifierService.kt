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

    @Scheduled(cron = "0 * * * * *")
    override suspend fun send() {
        val lockKey = CacheKey.LOCK_SEND_TIMETABLE_LECTURE_REMINDER_NOTIFICATION.build()
        cache.withLock(lockKey) {
            try {
                logger.debug("강의 리마인더 알림 전송 작업을 시작합니다.")
                val currentTime = Instant.now()
                val (currentYear, currentSemester) =
                    SemesterUtils.getCurrentYearAndSemester(currentTime) ?: run {
                        logger.debug("현재 진행 중인 학기가 없습니다.")
                        return@withLock
                    }
                val reminders = getTargetReminders(currentTime)

                if (reminders.isEmpty()) {
                    logger.debug("현재 시간대에 전송할 강의 리마인더가 없습니다.")
                    return@withLock
                }

                logger.info("총 ${reminders.size}개의 강의 리마인더를 찾았습니다.")
                processReminders(reminders, currentYear, currentSemester, currentTime)
            } catch (e: Exception) {
                logger.error("강의 리마인더 알림 전송 중 오류 발생", e)
            }
        }
    }

    private suspend fun getTargetReminders(currentTime: Instant): List<TimetableLectureReminder> {
        val endSchedule = TimetableLectureReminder.Schedule.fromInstant(currentTime)
        val startSchedule = endSchedule.minusMinutes(REMINDER_TIME_WINDOW_MINUTES.toInt())
        val lastNotifiedBefore = currentTime.minus(REMINDER_TIME_WINDOW_MINUTES, ChronoUnit.MINUTES)

        val reminders =
            if (startSchedule.day == endSchedule.day) {
                // 같은 날짜 내에서의 스케줄
                timetableLectureReminderRepository.findDueRemindersInTimeRange(
                    dayOfWeek = endSchedule.day.value,
                    startMinute = startSchedule.minute,
                    endMinute = endSchedule.minute,
                    lastNotifiedBefore = lastNotifiedBefore,
                )
            } else {
                // 다른 날짜(자정 즈음)
                timetableLectureReminderRepository.findDueRemindersInTimeRange( // 어제 스케줄
                    dayOfWeek = startSchedule.day.value,
                    startMinute = startSchedule.minute,
                    endMinute = 1439, // 23:59
                    lastNotifiedBefore = lastNotifiedBefore,
                ) +
                    timetableLectureReminderRepository.findDueRemindersInTimeRange( // 오늘 스케줄
                        dayOfWeek = endSchedule.day.value,
                        startMinute = 0,
                        endMinute = endSchedule.minute,
                        lastNotifiedBefore = lastNotifiedBefore,
                    )
            }

        return reminders
    }

    private suspend fun processReminders(
        reminders: List<TimetableLectureReminder>,
        currentYear: Int,
        currentSemester: Semester,
        currentTime: Instant,
    ) {
        reminders.chunked(BATCH_SIZE).forEachIndexed { index, chunkedReminders ->
            logger.debug("배치 ${index + 1} 처리 중: ${chunkedReminders.size}개의 리마인더")
            processChunkedReminders(chunkedReminders, currentYear, currentSemester, currentTime)
        }
    }

    private suspend fun processChunkedReminders(
        chunkedReminders: List<TimetableLectureReminder>,
        currentYear: Int,
        currentSemester: Semester,
        currentTime: Instant,
    ) {
        val timetablesById =
            timetableRepository.findAllById(chunkedReminders.map { it.timetableId }).toList().associateBy { it.id }
        val timetableAndReminders =
            chunkedReminders.mapNotNull { reminder ->
                timetablesById[reminder.timetableId]?.let { timetable ->
                    TimetableAndReminder(timetable, reminder)
                }
            }

        val currentSemesterPrimaryTimetableAndReminders =
            timetableAndReminders
                .filter {
                    it.timetable.year == currentYear && it.timetable.semester == currentSemester && it.timetable.isPrimary == true
                }
        sendPushes(currentSemesterPrimaryTimetableAndReminders)
        markRemindersAsNotified(currentSemesterPrimaryTimetableAndReminders.map { it.reminder }, currentTime)
        logger.info("${currentSemesterPrimaryTimetableAndReminders.size}개의 현재 학기 리마인더에 알림을 보냈습니다.")

        val pastSemesterTimetableAndReminders =
            timetableAndReminders.filter {
                it.timetable.year < currentYear || (it.timetable.year == currentYear && it.timetable.semester < currentSemester)
            }
        deletePastSemesterReminders(pastSemesterTimetableAndReminders.map { it.reminder })
    }

    private suspend fun sendPushes(targets: List<TimetableAndReminder>) {
        val userIdToPushMessage =
            targets
                .mapNotNull {
                    val pushMessage = buildPushMessage(it, it.reminder.offsetMinutes) ?: return@mapNotNull null
                    it.timetable.userId to pushMessage
                }.toMap()

        if (userIdToPushMessage.isNotEmpty()) {
            pushService.sendTargetPushes(userIdToPushMessage)
        }
    }

    private suspend fun markRemindersAsNotified(
        reminders: List<TimetableLectureReminder>,
        currentTime: Instant,
    ) {
        if (reminders.isEmpty()) return

        val scheduleEndTime = TimetableLectureReminder.Schedule.fromInstant(currentTime)
        val scheduleStartTime = scheduleEndTime.minusMinutes(REMINDER_TIME_WINDOW_MINUTES.toInt())

        val updatedReminders =
            reminders.map { reminder ->
                val newSchedules =
                    reminder.schedules.map { schedule ->
                        // 강의 하나의 schedule이 10분 윈도우 안에 여러 개라면
                        // 알림을 1분마다 하나씩 보낼 필요 없이 한 번만 보내고
                        // schedule은 전부 recentNotifiedAt을 기록하여 리소스를 아낀다.
                        if (schedule.shouldBeMarkedAsNotified(scheduleStartTime, scheduleEndTime, currentTime)) {
                            schedule.copy(recentNotifiedAt = currentTime)
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
        startSchedule: TimetableLectureReminder.Schedule,
        endSchedule: TimetableLectureReminder.Schedule,
        currentTime: Instant,
    ): Boolean {
        val isInTimeWindow = this.isWithin(startSchedule, endSchedule)
        val hasNotBeenNotifiedRecently =
            this.recentNotifiedAt == null ||
                this.recentNotifiedAt < currentTime.minus(REMINDER_TIME_WINDOW_MINUTES, ChronoUnit.MINUTES)

        return isInTimeWindow && hasNotBeenNotifiedRecently
    }

    private fun buildPushMessage(
        timetableAndReminder: TimetableAndReminder,
        offsetMinutes: Int,
    ): PushMessage? {
        val timetableLecture =
            timetableAndReminder.timetable.lectures.find { it.id == timetableAndReminder.reminder.timetableLectureId } ?: return null

        val typeString = if (timetableLecture.lectureId == null) "일정" else "수업"
        val pushTitle = "\uD83D\uDCDA $typeString 리마인더"
        val pushBody =
            when {
                offsetMinutes == 0 -> "${timetableLecture.courseTitle} $typeString 시간이에요."
                offsetMinutes > 0 -> "${timetableLecture.courseTitle} $typeString 시작 ${offsetMinutes}분 후예요."
                else -> "${timetableLecture.courseTitle} $typeString 시작 ${-offsetMinutes}분 전이에요."
            }

        return PushMessage(
            title = pushTitle,
            body = pushBody,
            isUrgentOnAndroid = true,
        )
    }
}
