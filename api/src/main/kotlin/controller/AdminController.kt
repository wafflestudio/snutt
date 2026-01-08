package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.clientconfig.dto.ConfigResponse
import com.wafflestudio.snutt.clientconfig.dto.PatchConfigRequest
import com.wafflestudio.snutt.clientconfig.dto.PostConfigRequest
import com.wafflestudio.snutt.clientconfig.service.ClientConfigService
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.storage.StorageService
import com.wafflestudio.snutt.common.storage.StorageSource
import com.wafflestudio.snutt.common.storage.dto.FileUploadUriDto
import com.wafflestudio.snutt.diary.data.DiaryDailyClassType
import com.wafflestudio.snutt.diary.data.DiaryQuestion
import com.wafflestudio.snutt.diary.dto.request.DiaryAddQuestionRequestDto
import com.wafflestudio.snutt.diary.service.DiaryService
import com.wafflestudio.snutt.filter.SnuttAdminApiFilterTarget
import com.wafflestudio.snutt.notification.service.NotificationAdminService
import com.wafflestudio.snutt.popup.dto.PopupResponse
import com.wafflestudio.snutt.popup.dto.PostPopupRequest
import com.wafflestudio.snutt.popup.service.PopupService
import notification.dto.InsertNotificationRequest
import org.springframework.http.MediaType
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
@SnuttAdminApiFilterTarget
@RequestMapping(
    "/v1/admin",
    "/admin",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class AdminController(
    private val notificationAdminService: NotificationAdminService,
    private val configService: ClientConfigService,
    private val storageService: StorageService,
    private val popupService: PopupService,
    private val diaryService: DiaryService,
) {
    @PostMapping("/insert_noti")
    suspend fun insertNotification(
        @RequestBody body: InsertNotificationRequest,
    ): OkResponse {
        notificationAdminService.insertNotification(body)
        return OkResponse()
    }

    @PostMapping("/configs/{name}")
    suspend fun postConfig(
        @PathVariable name: String,
        @RequestBody body: PostConfigRequest,
    ): ConfigResponse {
        val config = configService.postConfig(name, body)
        return ConfigResponse.from(config)
    }

    @GetMapping("/configs/{name}")
    suspend fun getConfigs(
        @PathVariable name: String,
    ): List<ConfigResponse> = configService.getConfigsByName(name).map { ConfigResponse.from(it) }

    @DeleteMapping("/configs/{name}/{id}")
    suspend fun deleteConfig(
        @PathVariable name: String,
        @PathVariable id: String,
    ) {
        configService.deleteConfig(name, id)
    }

    @PatchMapping("/configs/{name}/{id}")
    suspend fun patchConfig(
        @PathVariable name: String,
        @PathVariable id: String,
        @RequestBody body: PatchConfigRequest,
    ): ConfigResponse {
        val config = configService.patchConfig(name, id, body)
        return ConfigResponse.from(config)
    }

    @PostMapping("/images/{source}/upload-uris")
    suspend fun getUploadSignedUris(
        @PathVariable source: String,
        @RequestParam(defaultValue = "1") count: Int,
    ): List<FileUploadUriDto> =
        storageService.getUploadSignedUris(
            StorageSource.from(source) ?: throw IllegalArgumentException("Invalid source"),
            count,
        )

    @PostMapping("/popups")
    suspend fun postPopup(
        @RequestBody body: PostPopupRequest,
    ): PopupResponse = popupService.postPopup(body).let(::PopupResponse)

    @DeleteMapping("/popups/{id}")
    suspend fun deletePopup(
        @PathVariable id: String,
    ) {
        popupService.deletePopup(id)
    }

    @GetMapping("/diary/dailyClassTypes")
    suspend fun getAllDiaryDailyClassTypes(): List<DiaryDailyClassType> = diaryService.getAllDailyClassTypes()

    @GetMapping("/diary/questions")
    suspend fun getDiaryQuestions(): List<DiaryQuestion> = diaryService.getActiveQuestions()

    @PostMapping("/diary/dailyClassTypes")
    suspend fun insertDiaryDailyClassType(
        @RequestParam name: String,
    ): OkResponse {
        diaryService.addOrEnableDailyClassType(name)
        return OkResponse()
    }

    @DeleteMapping("/diary/dailyClassTypes")
    suspend fun removeDiaryDailyClassType(
        @RequestParam name: String,
    ): OkResponse {
        diaryService.disableDailyClassType(name)
        return OkResponse()
    }

    @PostMapping("/diary/questions")
    suspend fun insertDiaryQuestion(
        @RequestBody body: DiaryAddQuestionRequestDto,
    ): OkResponse {
        diaryService.addQuestion(body)
        return OkResponse()
    }

    @DeleteMapping("/diary/questions/{id}")
    suspend fun removeDiaryQuestion(
        @PathVariable id: String,
    ): OkResponse {
        diaryService.removeQuestion(id)
        return OkResponse()
    }
}
