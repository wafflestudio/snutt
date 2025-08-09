package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.util.SemesterUtils
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.timetablelecturereminder.dto.TimetableLectureReminderDto
import com.wafflestudio.snutt.timetablelecturereminder.dto.request.TimetableLectureReminderModifyRequestDto
import com.wafflestudio.snutt.timetablelecturereminder.dto.response.TimetableLectureRemindersWithTimetableIdResponse
import com.wafflestudio.snutt.timetablelecturereminder.service.TimetableLectureReminderService
import com.wafflestudio.snutt.timetables.service.TimetableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import java.time.Instant

@Component
class TimetableLectureReminderHandler(
    private val timetableLectureReminderService: TimetableLectureReminderService,
    private val timetableService: TimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getReminder(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableId = req.pathVariable("timetableId")
            val timetableLectureId = req.pathVariable("timetableLectureId")
            timetableLectureReminderService.getReminder(timetableId, timetableLectureId)?.let(::TimetableLectureReminderDto)
        }

    suspend fun getRemindersInActiveSemesterPrimaryTimetable(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val (activeYear, activeSemester) = SemesterUtils.getCurrentOrNextYearAndSemester(Instant.now())
            val primaryTimetable = timetableService.getUserPrimaryTable(userId, activeYear, activeSemester)
            val reminders = timetableLectureReminderService.getReminders(primaryTimetable.id!!)
            TimetableLectureRemindersWithTimetableIdResponse(
                timetableId = primaryTimetable.id!!,
                reminders = reminders.map(::TimetableLectureReminderDto),
            )
        }

    suspend fun modifyReminder(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableId = req.pathVariable("timetableId")
            val timetableLectureId = req.pathVariable("timetableLectureId")
            val body = req.awaitBody<TimetableLectureReminderModifyRequestDto>()
            timetableLectureReminderService
                .modifyReminder(
                    timetableId,
                    timetableLectureId,
                    body.offsetMinutes,
                ).let(::TimetableLectureReminderDto)
        }

    suspend fun deleteReminder(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableLectureId = req.pathVariable("timetableLectureId")
            timetableLectureReminderService.deleteReminder(timetableLectureId)
        }
}
