package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.exception.FriendNotFoundException
import com.wafflestudio.snu4t.friend.service.FriendService
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.dto.TimetableDto
import com.wafflestudio.snu4t.timetables.service.TimetableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class FriendTableHandler(
    private val friendService: FriendService,
    private val timetableService: TimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {

    suspend fun getPrimaryTable(req: ServerRequest) = handle(req) {
        val friend = friendService.get(req.pathVariable("friendId"))
            ?.takeIf { it.isAccepted && it.includes(req.userId) }
            ?: throw FriendNotFoundException

        val semester = req.parseRequiredQueryParam("semester") { Semester.getOfValue(it.toInt()) }
        val year = req.parseRequiredQueryParam<Int>("year")
        timetableService.getUserPrimaryTable(friend.getPartnerUserId(req.userId), year, semester).let(::TimetableDto)
    }

    suspend fun getCoursebooks(req: ServerRequest) = handle(req) {
        val friend = friendService.get(req.pathVariable("friendId"))
            ?.takeIf { it.isAccepted && it.includes(req.userId) }
            ?: throw FriendNotFoundException

        timetableService.getCoursebooksWithPrimaryTable(friend.getPartnerUserId(req.userId))
    }
}
