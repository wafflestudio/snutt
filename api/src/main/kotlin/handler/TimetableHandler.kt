package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.exception.InvalidPathParameterException
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.dto.TimetableDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableAddRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableModifyRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableModifyThemeRequestDto
import com.wafflestudio.snu4t.timetables.service.TimetableService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import timetables.dto.TimetableBriefDto

@Component
class TimetableHandler(
    private val timeTableService: TimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiDefaultMiddleware
) {
    suspend fun getTimetableBriefs(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId

        timeTableService.getTimetables(userId = userId).toList().map(::TimetableBriefDto)
    }

    suspend fun getMostRecentlyUpdatedTimetables(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId

        timeTableService.getMostRecentlyUpdatedTimetable(userId).let(::TimetableDto)
    }

    suspend fun getTimetablesBySemester(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val year = req.pathVariable("year").toInt()
        val semester =
            Semester.getOfValue(req.pathVariable("semester").toInt()) ?: throw InvalidPathParameterException("semester")

        timeTableService.getTimetablesBySemester(userId, year, semester).toList().map(::TimetableDto)
    }

    suspend fun addTimetable(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val sourceTimetableId = req.parseQueryParam<String>("source")
        val body = req.awaitBody<TimetableAddRequestDto>()

        if (sourceTimetableId == null) {
            timeTableService.addTimetable(userId, body)
        } else {
            timeTableService.copyTimetable(userId, sourceTimetableId)
        }
        timeTableService.getTimetables(userId = userId).toList().map(::TimetableBriefDto)
    }

    suspend fun getTimetable(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")

        timeTableService.getTimetable(userId, timetableId).let(::TimetableDto)
    }

    suspend fun modifyTimetable(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val body = req.awaitBody<TimetableModifyRequestDto>()

        timeTableService.modifyTimetableTitle(userId, timetableId, body.title)
        timeTableService.getTimetables(userId = userId).toList().map(::TimetableBriefDto)
    }

    suspend fun deleteTimetable(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")

        timeTableService.deleteTimetable(userId, timetableId)
        timeTableService.getTimetables(userId = userId).toList().map(::TimetableBriefDto)
    }

    suspend fun getTimetableLink(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        timeTableService.getTimetableLink(userId, timetableId)
    }

    suspend fun copyTimetable(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")

        timeTableService.copyTimetable(userId, timetableId)
        timeTableService.getTimetables(userId = userId).toList().map(::TimetableBriefDto)
    }

    suspend fun modifyTimetableTheme(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val theme = req.awaitBody<TimetableModifyThemeRequestDto>().theme

        timeTableService.modifyTimetableTheme(userId, timetableId, theme).let(::TimetableDto)
    }

    suspend fun addCustomLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        TODO("Not yet implemented")
    }

    suspend fun addLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val lectureId = req.pathVariable("lectureId")
        TODO("Not yet implemented")
    }

    suspend fun resetTimetableLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val timetableLectureId = req.pathVariable("timetableLectureId")
        TODO("Not yet implemented")
    }

    suspend fun modifyTimetableLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val timetableLectureId = req.pathVariable("timetableLectureId")
        ServerResponse.ok()
    }

    suspend fun deleteTimetableLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val lectureId = req.pathVariable("timetableLectureId")
        ServerResponse.ok()
    }
}
