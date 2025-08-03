package com.wafflestudio.snutt.timetablelecturereminder.service

import com.wafflestudio.snutt.common.exception.PastSemesterException
import com.wafflestudio.snutt.common.exception.TimetableLectureNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableNotFoundException
import com.wafflestudio.snutt.common.util.SemesterUtils
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import com.wafflestudio.snutt.timetablelecturereminder.repository.TimetableLectureReminderRepository
import com.wafflestudio.snutt.timetables.data.Timetable
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import org.springframework.stereotype.Service
import java.time.Instant

interface TimetableLectureReminderService {
    suspend fun getReminder(
        timetableId: String,
        timetableLectureId: String,
    ): TimetableLectureReminder?

    suspend fun getReminders(timetableId: String): List<TimetableLectureReminder>

    suspend fun modifyReminder(
        timetableId: String,
        timetableLectureId: String,
        offsetMinutes: Int,
    ): TimetableLectureReminder

    suspend fun deleteReminder(timetableLectureId: String)

    suspend fun updateScheduleIfNeeded(modifiedTimetableLecture: TimetableLecture)
}

@Service
class TimetableLectureReminderServiceImpl(
    private val timetableLectureReminderRepository: TimetableLectureReminderRepository,
    private val timetableRepository: TimetableRepository,
) : TimetableLectureReminderService {
    override suspend fun getReminder(
        timetableId: String,
        timetableLectureId: String,
    ): TimetableLectureReminder? {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        validateTimetableSemester(timetable)
        val reminder = timetableLectureReminderRepository.findByTimetableLectureId(timetableLectureId)
        return reminder
    }

    override suspend fun getReminders(timetableId: String): List<TimetableLectureReminder> {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        validateTimetableSemester(timetable)
        val reminders = timetableLectureReminderRepository.findByTimetableLectureIdIn(timetable.lectures.map { it.id })
        return reminders
    }

    override suspend fun modifyReminder(
        timetableId: String,
        timetableLectureId: String,
        offsetMinutes: Int,
    ): TimetableLectureReminder {
        val timetable = timetableRepository.findById(timetableId) ?: throw TimetableNotFoundException
        validateTimetableSemester(timetable)
        val timetableLecture =
            timetable.lectures.find { it.id == timetableLectureId } ?: throw TimetableLectureNotFoundException

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
        return timetableLectureReminderRepository.save(reminder)
    }

    override suspend fun deleteReminder(timetableLectureId: String) {
        val reminder =
            timetableLectureReminderRepository.findByTimetableLectureId(timetableLectureId) ?: return
        timetableLectureReminderRepository.delete(reminder)
    }

    override suspend fun updateScheduleIfNeeded(modifiedTimetableLecture: TimetableLecture) {
        val reminder =
            timetableLectureReminderRepository.findByTimetableLectureId(modifiedTimetableLecture.id)
                ?: return
        val existingSchedulesMap =
            reminder.schedules.associateBy { it.day to it.minute }
        val newSchedules =
            modifiedTimetableLecture.classPlaceAndTimes.map { classPlaceAndTime ->
                val newSchedule =
                    TimetableLectureReminder.Schedule(
                        classPlaceAndTime.day,
                        classPlaceAndTime.startMinute,
                    ).plusMinutes(reminder.offsetMinutes)
                val existingSchedule = existingSchedulesMap[newSchedule.day to newSchedule.minute]
                existingSchedule ?: newSchedule // 이미 알림을 보낸 schedule의 recentNotifiedAt은 유지하고, 새로 추가된 schedule에 대해서는 null로 설정
            }
        timetableLectureReminderRepository.save(reminder.copy(schedules = newSchedules))
    }

    private fun validateTimetableSemester(timetable: Timetable) {
        val (activeYear, activeSemester) = SemesterUtils.getCurrentOrNextYearAndSemester(Instant.now())
        if (timetable.year < activeYear || (timetable.year == activeYear && timetable.semester < activeSemester)) {
            throw PastSemesterException
        }
    }
}
