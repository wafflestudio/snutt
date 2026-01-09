package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.timetablelecturereminder.dto.TimetableLectureReminderDto
import com.wafflestudio.snutt.timetablelecturereminder.dto.request.TimetableLectureReminderModifyRequestDto
import com.wafflestudio.snutt.timetablelecturereminder.service.TimetableLectureReminderService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/tables/{timetableId}",
    "/tables/{timetableId}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class TimetableLectureReminderController(
    private val timetableLectureReminderService: TimetableLectureReminderService,
) {
    @GetMapping("/lecture/{timetableLectureId}/reminder")
    suspend fun getReminder(
        @PathVariable timetableId: String,
        @PathVariable timetableLectureId: String,
    ) = timetableLectureReminderService
        .getReminder(timetableId, timetableLectureId)
        .let(::TimetableLectureReminderDto)

    @GetMapping("/lecture/reminders")
    suspend fun getReminders(
        @PathVariable timetableId: String,
    ) = timetableLectureReminderService
        .getReminders(timetableId)
        .map(::TimetableLectureReminderDto)

    @PutMapping("/lecture/{timetableLectureId}/reminder")
    suspend fun modifyReminder(
        @PathVariable timetableId: String,
        @PathVariable timetableLectureId: String,
        @RequestBody body: TimetableLectureReminderModifyRequestDto,
    ) = timetableLectureReminderService
        .modifyReminder(
            timetableId,
            timetableLectureId,
            body.option,
        ).let(::TimetableLectureReminderDto)
}
