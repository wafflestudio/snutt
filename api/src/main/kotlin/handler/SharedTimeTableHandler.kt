package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimeTableCreateRequest
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimeTableModifyRequest
import com.wafflestudio.snu4t.sharedtimetable.service.SharedTimeTableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class SharedTimeTableHandler(
    private val sharedTimeTableService: SharedTimeTableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(handlerMiddleware = snuttRestApiDefaultMiddleware) {
    suspend fun getSharedTimeTables(req: ServerRequest) = handle(req) {
        val userId = req.userId
        sharedTimeTableService.gets(userId)
    }

    suspend fun getSharedTimeTable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val timeTableId = req.pathVariable("id")
        sharedTimeTableService.get(timeTableId)
    }

    suspend fun addSharedTimeTable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val requestBody = req.awaitBody<SharedTimeTableCreateRequest>()
        sharedTimeTableService.add(userId, requestBody.title, requestBody.timetableId)
        null
    }

    suspend fun updateSharedTimetable(req: ServerRequest) = handle(req) {
        val sharedTimetableId = req.pathVariable("id")
        val requestBody = req.awaitBody<SharedTimeTableModifyRequest>()
        sharedTimeTableService.update(requestBody.title, sharedTimetableId)
    }

    suspend fun deleteSharedTimeTable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val timeTableId = req.pathVariable("id")
        sharedTimeTableService.delete(userId, timeTableId)
        null
    }
}
