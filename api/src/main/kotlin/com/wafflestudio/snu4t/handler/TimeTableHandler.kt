package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.timetables.service.TimeTableService
import com.wafflestudio.snu4t.users.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class TimeTableHandler(private val timeTableService: TimeTableService, private val userService: UserService) : ServiceHandler() {
    suspend fun getTimeTables(req: ServerRequest): ServerResponse = handle {
        // TODO: 유저 가져오는 부분 분리부탁~~
        val token = req.headers().firstHeader("x-access-token") ?: throw AuthException
        val user = userService.getUserByCredentialHash(token)
        timeTableService.getTimeTablesOfUser(user).toResponse()
    }
}