package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.common.exception.InvalidPathParameterException
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.theme.dto.request.TimetableModifyThemeRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snutt.timetables.dto.request.TimetableModifyRequestDto
import com.wafflestudio.snutt.timetables.service.TimetableService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import timetables.dto.TimetableBriefDto

@Component
class TimetableHandler(
    private val timetableService: TimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
        handlerMiddleware = snuttRestApiDefaultMiddleware,
    ) {
    suspend fun getTimetableBriefs(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId

            timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
        }

    suspend fun getMostRecentlyUpdatedTimetables(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId

            timetableService.getMostRecentlyUpdatedTimetable(userId)
                .let { timetableService.convertTimetableToTimetableLegacyDto(it) }
        }

    suspend fun getTimetablesBySemester(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val year = req.pathVariable("year").toInt()
            val semester =
                Semester.getOfValue(req.pathVariable("semester").toInt()) ?: throw InvalidPathParameterException("semester")

            timetableService.getTimetablesBySemester(userId, year, semester).toList()
                .map { timetableService.convertTimetableToTimetableLegacyDto(it) }
        }

    suspend fun addTimetable(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val sourceTimetableId = req.parseQueryParam<String>("source")
            val body = req.awaitBody<TimetableAddRequestDto>()

            if (sourceTimetableId == null) {
                timetableService.addTimetable(userId, body)
            } else {
                timetableService.copyTimetable(userId, sourceTimetableId)
            }
            timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
        }

    suspend fun getTimetable(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val timetableId = req.pathVariable("timetableId")

            timetableService.getTimetable(userId, timetableId)
                .let { timetableService.convertTimetableToTimetableLegacyDto(it) }
        }

    suspend fun modifyTimetable(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val timetableId = req.pathVariable("timetableId")
            val body = req.awaitBody<TimetableModifyRequestDto>()

            timetableService.modifyTimetableTitle(userId, timetableId, body.title)
            timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
        }

    suspend fun deleteTimetable(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val timetableId = req.pathVariable("timetableId")

            timetableService.deleteTimetable(userId, timetableId)
            timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
        }

    suspend fun getTimetableLink(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val timetableId = req.pathVariable("timetableId")
            timetableService.getTimetableLink(userId, timetableId)
        }

    suspend fun copyTimetable(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val timetableId = req.pathVariable("timetableId")

            timetableService.copyTimetable(userId, timetableId)
            timetableService.getTimetables(userId = userId).map(::TimetableBriefDto)
        }

    suspend fun modifyTimetableTheme(req: ServerRequest): ServerResponse =
        handle(req) {
            val userId = req.userId
            val timetableId = req.pathVariable("timetableId")
            val body = req.awaitBody<TimetableModifyThemeRequestDto>()

            timetableService.modifyTimetableTheme(userId, timetableId, body.theme, body.themeId)
                .let { timetableService.convertTimetableToTimetableLegacyDto(it) }
        }

    suspend fun setPrimary(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableId = req.pathVariable("timetableId")
            timetableService.setPrimary(req.userId, timetableId)
        }

    suspend fun unSetPrimary(req: ServerRequest): ServerResponse =
        handle(req) {
            val timetableId = req.pathVariable("timetableId")
            timetableService.unSetPrimary(req.userId, timetableId)
        }
}
