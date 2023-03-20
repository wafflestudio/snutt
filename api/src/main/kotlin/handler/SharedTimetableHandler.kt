package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableCreateRequest
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimetableModifyRequest
import com.wafflestudio.snu4t.sharedtimetable.service.SharedTimetableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class SharedTimetableHandler(
    private val sharedTimetableService: SharedTimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(handlerMiddleware = snuttRestApiDefaultMiddleware) {
    suspend fun getSharedTimetables(req: ServerRequest) = handle(req) {
        val userId = req.userId
        sharedTimetableService.gets(userId)
    }

    suspend fun getSharedTimetable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val timeTableId = req.pathVariable("id")
        sharedTimetableService.get(timeTableId)
    }

    suspend fun addSharedTimetable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val requestBody = req.awaitBody<SharedTimetableCreateRequest>()
        sharedTimetableService.add(userId, requestBody.title, requestBody.timetableId)
        null
    }

    suspend fun updateSharedTimetable(req: ServerRequest) = handle(req) {
        val sharedTimetableId = req.pathVariable("id")
        val requestBody = req.awaitBody<SharedTimetableModifyRequest>()
        sharedTimetableService.update(requestBody.title, sharedTimetableId)
    }

    suspend fun deleteSharedTimetable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val timeTableId = req.pathVariable("id")
        sharedTimetableService.delete(userId, timeTableId)
        null
    }
}
