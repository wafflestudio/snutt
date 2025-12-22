package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.notification.dto.PushPreferenceDto
import com.wafflestudio.snutt.notification.service.PushPreferenceService
import com.wafflestudio.snutt.users.data.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/push/preferences", "/push/preferences")
class PushPreferenceController(
    private val pushPreferenceService: PushPreferenceService,
) {
    @GetMapping("")
    suspend fun getPushPreferences(
        @CurrentUser user: User,
    ) = pushPreferenceService.getPushPreferenceDto(user)

    @PostMapping("")
    suspend fun savePushPreferences(
        @CurrentUser user: User,
        @RequestBody pushPreferenceDto: PushPreferenceDto,
    ) = pushPreferenceService.savePushPreference(user, pushPreferenceDto)
}
