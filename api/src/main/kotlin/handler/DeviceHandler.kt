package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.OkResponse
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.notification.service.DeviceService
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.service.UserNicknameGenerateService
import com.wafflestudio.snu4t.users.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class DeviceHandler(
    private val deviceService: DeviceService,
    private val userService: UserService,
    private val userNicknameGenerateService: UserNicknameGenerateService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun addRegistrationId(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val clientInfo = req.clientInfo!!

        val registrationId = req.pathVariable("id")
        deviceService.addRegistrationId(userId, registrationId, clientInfo)

        updateIfUserNicknameNull(req.getContext().user!!)
        OkResponse()
    }

    // TODO 회원가입 API (SNUTT -> SNU4T) 마이그레이션 완료 이후 삭제
    //   context: https://wafflestudio.slack.com/archives/C0PAVPS5T/p1690711859658779
    private suspend fun updateIfUserNicknameNull(user: User) {
        if (user.nickname == null) {
            val nickname = userNicknameGenerateService.generateUniqueRandomNickname()
            userService.update(user.copy(nickname = nickname))
        }
    }

    suspend fun removeRegistrationId(req: ServerRequest) = handle(req) {
        val userId = req.userId

        val registrationId = req.pathVariable("id")
        deviceService.removeRegistrationId(userId, registrationId)

        OkResponse()
    }
}
