package com.wafflestudio.snutt.timetablelecturereminder.service

import com.wafflestudio.snutt.common.exception.InvalidTimeException
import com.wafflestudio.snutt.common.exception.TimetableLectureNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableNotFoundException
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureAndReminder
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import com.wafflestudio.snutt.timetablelecturereminder.dto.TimetableLectureReminderOption
import com.wafflestudio.snutt.timetablelecturereminder.repository.TimetableLectureReminderRepository
import com.wafflestudio.snutt.timetables.event.data.TimetableLectureModifiedEvent
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

interface TimetableLectureReminderService {
    suspend fun getReminder(
        timetableId: String,
        timetableLectureId: String,
    ): TimetableLectureAndReminder

    suspend fun getReminders(timetableId: String): List<TimetableLectureAndReminder>

    suspend fun modifyReminder(
        timetableId: String,
        timetableLectureId: String,
        option: TimetableLectureReminderOption,
    ): TimetableLectureAndReminder
}

@Service
class TimetableLectureReminderServiceImpl(
    private val timetableLectureReminderRepository: TimetableLectureReminderRepository,
    private val timetableRepository: TimetableRepository,
) : TimetableLectureReminderService {
    override suspend fun getReminder(
        timetableId: String,
        timetableLectureId: String,
    ): TimetableLectureAndReminder {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val lecture =
            timetable.lectures.find { it.id == timetableLectureId }
                ?: throw TimetableLectureNotFoundException
        val reminder = timetableLectureReminderRepository.findByTimetableLectureId(timetableLectureId)

        return TimetableLectureAndReminder(
            timetableLecture = lecture,
            reminder = reminder,
        )
    }

    override suspend fun getReminders(timetableId: String): List<TimetableLectureAndReminder> {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val reminders = timetableLectureReminderRepository.findByTimetableLectureIdIn(timetable.lectures.map { it.id })
        val reminderMap = reminders.associateBy { it.timetableLectureId }

        return timetable.lectures.map { lecture ->
            TimetableLectureAndReminder(
                timetableLecture = lecture,
                reminder = reminderMap[lecture.id],
            )
        }
    }

    override suspend fun modifyReminder(
        timetableId: String,
        timetableLectureId: String,
        option: TimetableLectureReminderOption,
    ): TimetableLectureAndReminder {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        val timetableLecture =
            timetable.lectures.find { it.id == timetableLectureId } ?: throw TimetableLectureNotFoundException

        if (timetableLecture.classPlaceAndTimes.isEmpty()) throw InvalidTimeException

        // '없음' 옵션인 경우 기존 리마인더 삭제
        if (option == TimetableLectureReminderOption.NONE) {
            timetableLectureReminderRepository.deleteByTimetableLectureId(timetableLecture.id)
            return TimetableLectureAndReminder(
                timetableLecture = timetableLecture,
                reminder = null,
            )
        }

        val offsetMinutes = option.offsetMinutes!!
        val schedules =
            timetableLecture.classPlaceAndTimes.map {
                TimetableLectureReminder.Schedule(it.day, it.startMinute).plusMinutes(offsetMinutes)
            }
        val reminder =
            timetableLectureReminderRepository.findByTimetableLectureId(timetableLectureId)?.copy(
                offsetMinutes = offsetMinutes,
                schedules = schedules,
            ) ?: TimetableLectureReminder(
                timetableId = timetable.id!!,
                timetableLectureId = timetableLectureId,
                offsetMinutes = offsetMinutes,
                schedules = schedules,
            )
        return TimetableLectureAndReminder(
            timetableLecture = timetableLecture,
            reminder = timetableLectureReminderRepository.save(reminder),
        )
    }

    @EventListener
    suspend fun handleTimetableLectureModifiedEvent(event: TimetableLectureModifiedEvent) {
        val modifiedTimetableLecture = event.timetableLecture
        if (modifiedTimetableLecture.classPlaceAndTimes.isEmpty()) { // 강의의 시간을 없애면 리마인더도 없앤다.
            timetableLectureReminderRepository.deleteByTimetableLectureId(modifiedTimetableLecture.id)
            return
        }

        val reminder =
            timetableLectureReminderRepository.findByTimetableLectureId(modifiedTimetableLecture.id)
                ?: return

        val existingSchedulesMap =
            reminder.schedules.associateBy { it.day to it.minute }
        val newSchedules =
            modifiedTimetableLecture.classPlaceAndTimes.map { classPlaceAndTime ->
                val newSchedule =
                    TimetableLectureReminder
                        .Schedule(
                            classPlaceAndTime.day,
                            classPlaceAndTime.startMinute,
                        ).plusMinutes(reminder.offsetMinutes)
                val existingSchedule = existingSchedulesMap[newSchedule.day to newSchedule.minute]
                existingSchedule ?: newSchedule // 이미 알림을 보낸 schedule의 recentNotifiedAt은 유지하고, 새로 추가된 schedule에 대해서는 null로 설정
            }
        if (newSchedules == reminder.schedules) return
        timetableLectureReminderRepository.save(reminder.copy(schedules = newSchedules))
    }
}
