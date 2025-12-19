package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.FriendNotFoundException
import com.wafflestudio.snutt.friend.service.FriendService
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.timetables.dto.TimetableDto
import com.wafflestudio.snutt.timetables.service.TimetableService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class FriendTableHandler(
    private val friendService: FriendService,
    private val timetableService: TimetableService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getPrimaryTable(req: ServerRequest) =
        handle(req) {
            val friend =
                friendService
                    .get(req.pathVariable("friendId"))
                    ?.takeIf { it.isAccepted && it.includes(req.userId) }
                    ?: throw FriendNotFoundException

            val semester = req.parseRequiredQueryParam("semester") { Semester.getOfValue(it.toInt()) }
            val year = req.parseRequiredQueryParam<Int>("year")
            timetableService.getUserPrimaryTable(friend.getPartnerUserId(req.userId), year, semester).let(::TimetableDto)
        }

    suspend fun getCoursebooks(req: ServerRequest) =
        handle(req) {
            val friend =
                friendService
                    .get(req.pathVariable("friendId"))
                    ?.takeIf { it.isAccepted && it.includes(req.userId) }
                    ?: throw FriendNotFoundException

            timetableService.getCoursebooksWithPrimaryTable(friend.getPartnerUserId(req.userId))
        }
}
