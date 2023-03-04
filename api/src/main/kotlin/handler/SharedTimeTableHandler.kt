package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.sharedtimetable.dto.SharedTimeTableModifyRequest
import com.wafflestudio.snu4t.sharedtimetable.service.SharedTimeTableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class SharedTimeTableHandler(
    private val sharedTimeTableService: SharedTimeTableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
): ServiceHandler(handlerMiddleware = snuttRestApiDefaultMiddleware) {
    suspend fun getSharedTimeTables(req: ServerRequest) = handle(req) {
        val userId = req.userId
        sharedTimeTableService.getSharedTimeTables(userId)
    }

    suspend fun getSharedTimeTable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val timeTableId = req.pathVariable("id")
        sharedTimeTableService.getSharedTimeTable(userId, timeTableId)
    }

    suspend fun addSharedTimeTable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val requestBody = req.awaitBody<SharedTimeTableModifyRequest>()
        sharedTimeTableService.addSharedTimeTable(userId, requestBody.title, requestBody.timetableId)
        null
    }

    suspend fun deleteSharedTimeTable(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val timeTableId = req.pathVariable("id")
        sharedTimeTableService.deleteSharedTimeTable(userId, timeTableId)
        null
    }
}