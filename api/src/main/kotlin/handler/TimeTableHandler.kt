package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.context
import com.wafflestudio.snu4t.timetables.service.TimeTableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class TimeTableHandler(
    private val timeTableService: TimeTableService,
) {

    suspend fun getTimeTables(req: ServerRequest): ServerResponse {
        val user = req.context().user ?: throw AuthException
        val response = timeTableService.getTimeTablesOfUser(user)

        return ServerResponse.ok().bodyValueAndAwait(response)
    }
}
