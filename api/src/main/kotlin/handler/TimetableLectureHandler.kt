package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.dto.request.CustomTimetableLectureAddLegacyRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableLectureModifyLegacyRequestDto
import com.wafflestudio.snu4t.timetables.service.TimetableLectureService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class TimetableLectureHandler(
    private val timetableLectureService: TimetableLectureService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(
    handlerMiddleware = snuttRestApiDefaultMiddleware
) {
    suspend fun addCustomLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val customTimetable = req.awaitBody<CustomTimetableLectureAddLegacyRequestDto>()
        val isForced = customTimetable.isForced

        timetableLectureService.addCustomTimetableLecture(
            userId = userId,
            timetableId = timetableId,
            timetableLectureRequest = customTimetable,
            isForced = isForced,
        )
    }

    suspend fun addLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val lectureId = req.pathVariable("lectureId")
        val isForced = req.awaitBody<ForcedReq>().isForced

        timetableLectureService.addLecture(
            userId = userId,
            timetableId = timetableId,
            lectureId = lectureId,
            isForced = isForced,
        )
    }

    suspend fun resetTimetableLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val timetableLectureId = req.pathVariable("timetableLectureId")

        timetableLectureService.resetTimetableLecture(
            userId = userId,
            timetableId = timetableId,
            timetableLectureId = timetableLectureId,
        )
    }

    suspend fun modifyTimetableLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val timetableLectureId = req.pathVariable("timetableLectureId")
        val modifyRequestDto = req.awaitBody<TimetableLectureModifyLegacyRequestDto>()
        val isForced = modifyRequestDto.isForced

        timetableLectureService.modifyTimetableLecture(
            userId = userId,
            timetableId = timetableId,
            modifyTimetableLectureRequestDto = modifyRequestDto,
            isForced = isForced,
        )
    }

    suspend fun deleteTimetableLecture(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val timetableId = req.pathVariable("timetableId")
        val timetableLectureId = req.pathVariable("timetableLectureId")

        timetableLectureService.deleteTimetableLecture(
            userId = userId,
            timetableId = timetableId,
            timetableLectureId = timetableLectureId,
        )
    }

    data class ForcedReq(val isForced: Boolean)
}
