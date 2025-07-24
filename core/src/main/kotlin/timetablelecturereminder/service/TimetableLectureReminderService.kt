package com.wafflestudio.snutt.timetablelecturereminder.service

import com.wafflestudio.snutt.common.exception.PastSemesterException
import com.wafflestudio.snutt.common.exception.PrimaryTimetableNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableLectureNotFoundException
import com.wafflestudio.snutt.common.exception.TimetableNotFoundException
import com.wafflestudio.snutt.common.util.SemesterUtils
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureReminder
import com.wafflestudio.snutt.timetablelecturereminder.data.TimetableLectureRemindersWithTimetable
import com.wafflestudio.snutt.timetablelecturereminder.repository.TimetableLectureReminderRepository
import com.wafflestudio.snutt.timetables.data.Timetable
import com.wafflestudio.snutt.timetables.data.TimetableLecture
import com.wafflestudio.snutt.timetables.repository.TimetableRepository
import org.springframework.stereotype.Service

interface TimetableLectureReminderService {
    suspend fun getReminder(
        timetableId: String,
        timetableLectureId: String,
    ): TimetableLectureReminder?

    suspend fun getRemindersInCurrentSemesterPrimaryTimetable(userId: String): TimetableLectureRemindersWithTimetable

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

    override suspend fun getRemindersInCurrentSemesterPrimaryTimetable(userId: String): TimetableLectureRemindersWithTimetable {
        val (currentYear, currentSemester) = SemesterUtils.getCurrentOrNextYearAndSemester()
        val primaryTimetable =
            timetableRepository.findByUserIdAndYearAndSemesterAndIsPrimaryTrue(userId, currentYear, currentSemester)
                ?: throw PrimaryTimetableNotFoundException
        val reminders =
            timetableLectureReminderRepository.findByTimetableLectureIdIn(primaryTimetable.lectures.map { it.id })
        return TimetableLectureRemindersWithTimetable(primaryTimetable, reminders)
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
                TimetableLectureReminder.Schedule(it.day, it.startMinute) + offsetMinutes
            }
        val reminder =
            timetableLectureReminderRepository.findByTimetableLectureId(timetableLectureId)?.copy(
                offsetMinutes = offsetMinutes,
                schedules = schedules,
            ) ?: TimetableLectureReminder(
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
            reminder.schedules.associateBy(
                keySelector = { it.day to it.minute },
                valueTransform = { it.notifiedAt },
            )
        val newSchedules =
            modifiedTimetableLecture.classPlaceAndTimes.map { classPlaceAndTime ->
                val newSchedule =
                    TimetableLectureReminder.Schedule(
                        classPlaceAndTime.day,
                        classPlaceAndTime.startMinute,
                    ) + reminder.offsetMinutes

                // 이미 알림을 보낸 시간은 유지하고, 새로 추가된 시간에 대해서는 null로 설정
                newSchedule.copy(notifiedAt = existingSchedulesMap[newSchedule.day to newSchedule.minute])
            }
        timetableLectureReminderRepository.save(reminder.copy(schedules = newSchedules))
    }

    private fun validateTimetableSemester(timetable: Timetable) {
        val (currentYear, currentSemester) = SemesterUtils.getCurrentOrNextYearAndSemester()
        if (timetable.year < currentYear || (timetable.year == currentYear && timetable.semester < currentSemester)) {
            throw PastSemesterException
        }
    }
}
