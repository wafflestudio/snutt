package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.theme.dto.request.TimetableModifyThemeRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableModifyRequestDto
import com.wafflestudio.snutt.timetables.service.TimetableService
import com.wafflestudio.snutt.users.data.User
import kotlinx.coroutines.flow.toList
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import timetables.dto.TimetableBriefDto

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/tables",
    "/tables",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class TimetableController(
    private val timetableService: TimetableService,
) {
    @GetMapping("")
    suspend fun getTimetableBriefs(
        @CurrentUser user: User,
    ): List<TimetableBriefDto> = timetableService.getTimetables(userId = user.id!!).map(::TimetableBriefDto)

    @GetMapping("/recent")
    suspend fun getMostRecentlyUpdatedTimetables(
        @CurrentUser user: User,
    ) = timetableService
        .getMostRecentlyUpdatedTimetable(user.id!!)
        .let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @GetMapping("/{year}/{semester}")
    suspend fun getTimetablesBySemester(
        @CurrentUser user: User,
        @PathVariable year: Int,
        @PathVariable semester: Semester,
    ) = timetableService
        .getTimetablesBySemester(
            user.id!!,
            year,
            semester,
        ).toList()
        .map { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @PostMapping("")
    suspend fun addTimetable(
        @CurrentUser user: User,
        @RequestParam(required = false) source: String?,
        @RequestBody body: TimetableAddRequestDto,
    ): List<TimetableBriefDto> {
        val userId = user.id!!
        if (source == null) {
            timetableService.addTimetable(userId, body)
        } else {
            timetableService.copyTimetable(userId, source)
        }
        return timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
    }

    @GetMapping("/{timetableId}")
    suspend fun getTimetable(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
    ) = timetableService
        .getTimetable(user.id!!, timetableId)
        .let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @PutMapping("/{timetableId}")
    suspend fun modifyTimetable(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @RequestBody body: TimetableModifyRequestDto,
    ): List<TimetableBriefDto> {
        val userId = user.id!!
        timetableService.modifyTimetableTitle(userId, timetableId, body.title)
        return timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
    }

    @DeleteMapping("/{timetableId}")
    suspend fun deleteTimetable(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
    ): List<TimetableBriefDto> {
        val userId = user.id!!
        timetableService.deleteTimetable(userId, timetableId)
        return timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
    }

    @PostMapping("/{timetableId}/copy")
    suspend fun copyTimetable(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
    ): List<TimetableBriefDto> {
        val userId = user.id!!
        timetableService.copyTimetable(userId, timetableId)
        return timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
    }

    @PutMapping("/{timetableId}/theme")
    suspend fun modifyTimetableTheme(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
        @RequestBody body: TimetableModifyThemeRequestDto,
    ) = timetableService
        .modifyTimetableTheme(user.id!!, timetableId, body.theme, body.themeId)
        .let { timetableService.convertTimetableToTimetableLegacyDto(it) }

    @PostMapping("/{timetableId}/primary")
    suspend fun setPrimary(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
    ) = timetableService.setPrimary(user.id!!, timetableId)

    @DeleteMapping("/{timetableId}/primary")
    suspend fun unSetPrimary(
        @CurrentUser user: User,
        @PathVariable timetableId: String,
    ) = timetableService.unSetPrimary(user.id!!, timetableId)
}
