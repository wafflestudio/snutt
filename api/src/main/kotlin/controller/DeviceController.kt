package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.exception.InvalidPathParameterException
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.notification.service.DeviceService
import com.wafflestudio.snutt.users.data.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping("/v1/user/device", "/user/device")
class DeviceController(
    private val deviceService: DeviceService,
) {
    @PostMapping("/{id}")
    suspend fun addRegistrationId(
        @CurrentUser user: User,
        @PathVariable id: String,
        @RequestAttribute("clientInfo") clientInfo: ClientInfo,
    ): OkResponse {
        if (id.isBlank()) throw InvalidPathParameterException("id")
        deviceService.addRegistrationId(user.id!!, id, clientInfo)
        return OkResponse()
    }

    @DeleteMapping("/{id}")
    suspend fun removeRegistrationId(
        @CurrentUser user: User,
        @PathVariable id: String,
    ): OkResponse {
        if (id.isBlank()) throw InvalidPathParameterException("id")
        deviceService.removeRegistrationId(user.id!!, id)
        return OkResponse()
    }
}
