package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.timetablelecturereminder.dto.TimetableLectureReminderDto
import com.wafflestudio.snutt.timetablelecturereminder.dto.request.TimetableLectureReminderModifyRequestDto
import com.wafflestudio.snutt.timetablelecturereminder.service.TimetableLectureReminderService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class TimetableLectureReminderHandler(
    private val timetableLectureReminderService: TimetableLectureReminderService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getReminder(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableId = req.pathVariable("timetableId")
            val timetableLectureId = req.pathVariable("timetableLectureId")

            timetableLectureReminderService
                .getReminder(timetableId, timetableLectureId)
                .let(::TimetableLectureReminderDto)
        }

    suspend fun getReminders(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableId = req.pathVariable("timetableId")

            timetableLectureReminderService
                .getReminders(timetableId)
                .map(::TimetableLectureReminderDto)
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
                    body.option,
                ).let(::TimetableLectureReminderDto)
        }
}
