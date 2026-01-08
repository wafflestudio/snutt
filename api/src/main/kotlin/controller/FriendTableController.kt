package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.FriendNotFoundException
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.coursebook.data.CoursebookDto
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.friend.service.FriendService
import com.wafflestudio.snutt.timetables.dto.TimetableDto
import com.wafflestudio.snutt.timetables.service.TimetableService
import com.wafflestudio.snutt.users.data.User
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/friends/{friendId}",
    "/friends/{friendId}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class FriendTableController(
    private val friendService: FriendService,
    private val timetableService: TimetableService,
) {
    @GetMapping("/primary-table")
    suspend fun getPrimaryTable(
        @CurrentUser user: User,
        @PathVariable friendId: String,
        @RequestParam semester: Semester,
        @RequestParam year: Int,
    ): TimetableDto {
        val userId = user.id!!
        val friend =
            friendService
                .get(friendId)
                ?.takeIf { it.isAccepted && it.includes(userId) }
                ?: throw FriendNotFoundException

        return timetableService
            .getUserPrimaryTable(friend.getPartnerUserId(userId), year, semester)
            .let(::TimetableDto)
    }

    @GetMapping("/coursebooks")
    suspend fun getCoursebooks(
        @CurrentUser user: User,
        @PathVariable friendId: String,
    ): List<CoursebookDto> {
        val userId = user.id!!
        val friend =
            friendService
                .get(friendId)
                ?.takeIf { it.isAccepted && it.includes(userId) }
                ?: throw FriendNotFoundException

        return timetableService.getCoursebooksWithPrimaryTable(friend.getPartnerUserId(userId))
    }

    @GetMapping("/registered-course-books")
    suspend fun getCoursebooksLegacy(
        @CurrentUser user: User,
        @PathVariable friendId: String,
    ) = getCoursebooks(user, friendId)
}
