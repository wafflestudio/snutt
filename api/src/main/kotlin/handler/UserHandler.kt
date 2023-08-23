package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.extension.toZonedDateTime
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.dto.UserDto
import com.wafflestudio.snu4t.users.dto.UserLegacyDto
import com.wafflestudio.snu4t.users.dto.UserPatchRequest
import com.wafflestudio.snu4t.users.service.UserNicknameService
import com.wafflestudio.snu4t.users.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class UserHandler(
    private val userService: UserService,
    private val userNicknameService: UserNicknameService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getUserMe(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId

        val user = userService.getUser(userId)
        buildUserDto(user)
    }

    suspend fun getUserInfo(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val user = userService.getUser(userId)

        UserLegacyDto(
            isAdmin = user.isAdmin,
            regDate = user.regDate.toZonedDateTime(),
            notificationCheckedAt = user.notificationCheckedAt.toZonedDateTime(),
            email = user.email,
            localId = user.credential.localId,
            fbName = user.credential.fbName,
        )
    }

    suspend fun patchUserInfo(req: ServerRequest): ServerResponse = handle(req) {
        val userId = req.userId
        val body = req.awaitBody<UserPatchRequest>()

        val user = userService.patchUserInfo(userId, body)
        buildUserDto(user)
    }

    private fun buildUserDto(user: User) = UserDto(
        isAdmin = user.isAdmin,
        regDate = user.regDate,
        notificationCheckedAt = user.notificationCheckedAt,
        email = user.email,
        localId = user.credential.localId,
        fbName = user.credential.fbName,
        nickname = userNicknameService.getNicknameDto(user.nickname),
    )
}
