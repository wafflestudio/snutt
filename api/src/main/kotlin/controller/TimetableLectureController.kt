package com.wafflestudio.snutt.controller

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.timetables.dto.request.CustomTimetableLectureAddLegacyRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableLectureModifyLegacyRequestDto
import com.wafflestudio.snutt.timetables.service.TimetableLectureService
import com.wafflestudio.snutt.timetables.service.TimetableService
import com.wafflestudio.snutt.users.data.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("/v1/tables/{timetableId}/lecture", "/tables/{timetableId}/lecture")
class TimetableLectureController(
    private val timetableLectureService: TimetableLectureService,
    private val timetableService: TimetableService,
) {
    @PostMapping("")
    suspend fun addCustomLecture(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @RequestParam(required = false) isForced: Boolean?,
        @RequestBody customTimetable: CustomTimetableLectureAddLegacyRequestDto,
    ) = timetableLectureService
        .addCustomTimetableLecture(
            userId = user.id!!,
            timetableId = timetableId,
            timetableLectureRequest = customTimetable,
            isForced = isForced ?: customTimetable.isForced,
        ).let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @PostMapping("/{lectureId}")
    suspend fun addLecture(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @PathVariable lectureId: String,
        @RequestParam(required = false) isForced: Boolean?,
        @RequestBody(required = false) body: ForcedReq?,
    ) = timetableLectureService
        .addLecture(
            userId = user.id!!,
            timetableId = timetableId,
            lectureId = lectureId,
            isForced = isForced ?: body?.isForced ?: false,
        ).let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @PutMapping("/{timetableLectureId}/reset")
    suspend fun resetTimetableLecture(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @PathVariable timetableLectureId: String,
        @RequestParam(required = false) isForced: Boolean?,
        @RequestBody(required = false) body: ForcedReq?,
    ) = timetableLectureService
        .resetTimetableLecture(
            userId = user.id!!,
            timetableId = timetableId,
            timetableLectureId = timetableLectureId,
            isForced = isForced ?: body?.isForced ?: false,
        ).let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @PutMapping("/{timetableLectureId}")
    suspend fun modifyTimetableLecture(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @PathVariable timetableLectureId: String,
        @RequestParam(required = false) isForced: Boolean?,
        @RequestBody modifyRequestDto: TimetableLectureModifyLegacyRequestDto,
    ) = timetableLectureService
        .modifyTimetableLecture(
            userId = user.id!!,
            timetableId = timetableId,
            timetableLectureId = timetableLectureId,
            modifyTimetableLectureRequestDto = modifyRequestDto,
            isForced = isForced ?: modifyRequestDto.isForced,
        ).let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @DeleteMapping("/{timetableLectureId}")
    suspend fun deleteTimetableLecture(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @PathVariable timetableLectureId: String,
    ) = timetableLectureService
        .deleteTimetableLecture(
            userId = user.id!!,
            timetableId = timetableId,
            timetableLectureId = timetableLectureId,
        ).let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    data class ForcedReq(
        @param:JsonProperty("is_forced")
        val isForced: Boolean?,
    )
}
