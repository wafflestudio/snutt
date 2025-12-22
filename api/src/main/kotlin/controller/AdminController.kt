package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.clientconfig.dto.ConfigResponse
import com.wafflestudio.snutt.clientconfig.dto.PatchConfigRequest
import com.wafflestudio.snutt.clientconfig.dto.PostConfigRequest
import com.wafflestudio.snutt.clientconfig.service.ClientConfigService
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.exception.UserNotAdminException
import com.wafflestudio.snutt.common.storage.StorageService
import com.wafflestudio.snutt.common.storage.StorageSource
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.service.DiaryService
import com.wafflestudio.snutt.notification.service.NotificationAdminService
import com.wafflestudio.snutt.popup.dto.PopupResponse
import com.wafflestudio.snutt.popup.dto.PostPopupRequest
import com.wafflestudio.snutt.popup.service.PopupService
import com.wafflestudio.snutt.users.data.User
import notification.dto.InsertNotificationRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin", "/admin")
class AdminController(
    private val notificationAdminService: NotificationAdminService,
    private val configService: ClientConfigService,
    private val storageService: StorageService,
    private val popupService: PopupService,
    private val diaryService: DiaryService,
) {
    private fun checkAdminPermission(user: User) {
        if (!user.isAdmin) {
            throw UserNotAdminException
        }
    }

    @PostMapping("/insert_noti")
    suspend fun insertNotification(
        @CurrentUser user: User,
        @RequestBody body: InsertNotificationRequest,
    ): OkResponse {
        checkAdminPermission(user)
        notificationAdminService.insertNotification(body)
        return OkResponse()
    }

    @PostMapping("/configs/{name}")
    suspend fun postConfig(
        @CurrentUser user: User,
        @PathVariable name: String,
        @RequestBody body: PostConfigRequest,
    ): ConfigResponse {
        checkAdminPermission(user)
        val config = configService.postConfig(name, body)
        return ConfigResponse.from(config)
    }

    @GetMapping("/configs/{name}")
    suspend fun getConfigs(
        @CurrentUser user: User,
        @PathVariable name: String,
    ): List<ConfigResponse> {
        checkAdminPermission(user)
        return configService.getConfigsByName(name).map { ConfigResponse.from(it) }
    }

    @DeleteMapping("/configs/{name}/{id}")
    suspend fun deleteConfig(
        @CurrentUser user: User,
        @PathVariable name: String,
        @PathVariable id: String,
    ) {
        checkAdminPermission(user)
        configService.deleteConfig(name, id)
    }

    @PatchMapping("/configs/{name}/{id}")
    suspend fun patchConfig(
        @CurrentUser user: User,
        @PathVariable name: String,
        @PathVariable id: String,
        @RequestBody body: PatchConfigRequest,
    ): ConfigResponse {
        checkAdminPermission(user)
        val config = configService.patchConfig(name, id, body)
        return ConfigResponse.from(config)
    }

    @PostMapping("/images/{source}/upload-uris")
    suspend fun getUploadSignedUris(
        @CurrentUser user: User,
        @PathVariable source: String,
        @RequestParam(defaultValue = "1") count: Int,
    ): Any {
        checkAdminPermission(user)
        return storageService.getUploadSignedUris(
            StorageSource.from(source) ?: throw IllegalArgumentException("Invalid source"),
            count,
        )
    }

    @PostMapping("/popups")
    suspend fun postPopup(
        @CurrentUser user: User,
        @RequestBody body: PostPopupRequest,
    ): PopupResponse {
        checkAdminPermission(user)
        return popupService.postPopup(body).let(::PopupResponse)
    }

    @DeleteMapping("/popups/{id}")
    suspend fun deletePopup(
        @CurrentUser user: User,
        @PathVariable id: String,
    ) {
        checkAdminPermission(user)
        popupService.deletePopup(id)
    }

    @GetMapping("/diary/dailyClassTypes")
    suspend fun getAllDiaryDailyClassTypes(
        @CurrentUser user: User,
    ): Any {
        checkAdminPermission(user)
        return diaryService.getAllDailyClassTypes()
    }

    @GetMapping("/diary/questions")
    suspend fun getDiaryQuestions(
        @CurrentUser user: User,
    ): Any {
        checkAdminPermission(user)
        return diaryService.getActiveQuestions()
    }

    @PostMapping("/diary/dailyClassTypes")
    suspend fun insertDiaryDailyClassType(
        @CurrentUser user: User,
        @RequestParam name: String,
    ): OkResponse {
        checkAdminPermission(user)
        diaryService.addOrEnableDailyClassType(name)
        return OkResponse()
    }

    @DeleteMapping("/diary/dailyClassTypes")
    suspend fun removeDiaryDailyClassType(
        @CurrentUser user: User,
        @RequestParam name: String,
    ): OkResponse {
        checkAdminPermission(user)
        diaryService.disableDailyClassType(name)
        return OkResponse()
    }

    @PostMapping("/diary/questions")
    suspend fun insertDiaryQuestion(
        @CurrentUser user: User,
        @RequestBody body: DiaryAddQuestionRequestDto,
    ): OkResponse {
        checkAdminPermission(user)
        diaryService.addQuestion(body)
        return OkResponse()
    }

    @DeleteMapping("/diary/questions/{id}")
    suspend fun removeDiaryQuestion(
        @CurrentUser user: User,
        @PathVariable id: String,
    ): OkResponse {
        checkAdminPermission(user)
        diaryService.removeQuestion(id)
        return OkResponse()
    }
}
