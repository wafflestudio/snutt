package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.enums.BasicThemeType
import com.wafflestudio.snutt.common.exception.InvalidPathParameterException
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.theme.dto.TimetableThemeDto
import com.wafflestudio.snutt.theme.dto.request.TimetableThemeAddRequestDto
import com.wafflestudio.snutt.theme.dto.request.TimetableThemeDownloadRequestDto
import com.wafflestudio.snutt.theme.dto.request.TimetableThemeModifyRequestDto
import com.wafflestudio.snutt.theme.dto.request.TimetableThemePublishRequestDto
import com.wafflestudio.snutt.theme.service.TimetableThemeService
import com.wafflestudio.snutt.users.data.User
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
@SnuttDefaultApiFilterTarget
@RequestMapping("/v1/themes", "/themes")
class TimetableThemeController(
    private val timetableThemeService: TimetableThemeService,
) {
    @GetMapping("")
    suspend fun getThemes(
        @CurrentUser user: User,
    ) = timetableThemeService.getThemes(user.id!!).map(::TimetableThemeDto)

    @GetMapping("/best")
    suspend fun getBestThemes(
        @RequestParam page: Int,
    ): ListResponse<*> {
        val themes = timetableThemeService.getBestThemes(page)
        val result = timetableThemeService.convertThemesToTimetableDtos(themes)
        return ListResponse(result)
    }

    @GetMapping("/friends")
    suspend fun getFriendsThemes(
        @CurrentUser user: User,
        @RequestParam page: Int,
    ): ListResponse<*> {
        val themes = timetableThemeService.getFriendsThemes(user.id!!, page)
        val result = timetableThemeService.convertThemesToTimetableDtos(themes)
        return ListResponse(result)
    }

    @GetMapping("/{themeId}")
    suspend fun getTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
    ) = TimetableThemeDto(timetableThemeService.getTheme(user.id!!, themeId))

    @PostMapping("")
    suspend fun addTheme(
        @CurrentUser user: User,
        @RequestBody body: TimetableThemeAddRequestDto,
    ) = timetableThemeService.addTheme(user.id!!, body.name, body.colors).let(::TimetableThemeDto)

    @PatchMapping("/{themeId}")
    suspend fun modifyTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
        @RequestBody body: TimetableThemeModifyRequestDto,
    ) = timetableThemeService.modifyTheme(user.id!!, themeId, body.name, body.colors).let(::TimetableThemeDto)

    @PostMapping("/{themeId}/publish")
    suspend fun publishTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
        @RequestBody body: TimetableThemePublishRequestDto,
    ): OkResponse {
        timetableThemeService.publishTheme(user.id!!, themeId, body.publishName, body.isAnonymous)
        return OkResponse()
    }

    @PostMapping("/{themeId}/download")
    suspend fun downloadTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
        @RequestBody body: TimetableThemeDownloadRequestDto,
    ) = TimetableThemeDto(timetableThemeService.downloadTheme(user.id!!, themeId, body.name))

    @PostMapping("/search")
    suspend fun searchThemes(
        @RequestParam query: String,
    ): ListResponse<*> {
        val themes = timetableThemeService.searchThemes(query)
        val result = timetableThemeService.convertThemesToTimetableDtos(themes)
        return ListResponse(result)
    }

    @DeleteMapping("/{themeId}")
    suspend fun deleteTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
    ) = timetableThemeService.deleteTheme(user.id!!, themeId)

    @DeleteMapping("/{themeId}/publish")
    suspend fun deletePublishedTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
    ) = timetableThemeService.deletePublishedTheme(user.id!!, themeId)

    @PostMapping("/{themeId}/copy")
    suspend fun copyTheme(
        @CurrentUser user: User,
        @PathVariable themeId: String,
    ) = timetableThemeService.copyTheme(user.id!!, themeId).let(::TimetableThemeDto)

    @PostMapping("/{themeId}/default")
    suspend fun setDefault(
        @CurrentUser user: User,
        @PathVariable themeId: String,
    ) = timetableThemeService.setDefault(user.id!!, themeId).let(::TimetableThemeDto)

    @PostMapping("/basic/{basicThemeTypeValue}/default")
    suspend fun setBasicThemeTypeDefault(
        @CurrentUser user: User,
        @PathVariable basicThemeTypeValue: Int,
    ): TimetableThemeDto {
        val basicThemeType =
            BasicThemeType.from(basicThemeTypeValue)
                ?: throw InvalidPathParameterException("basicThemeTypeValue")
        return timetableThemeService.setDefault(user.id!!, basicThemeType = basicThemeType).let(::TimetableThemeDto)
    }

    @DeleteMapping("/{themeId}/default")
    suspend fun unsetDefault(
        @CurrentUser user: User,
        @PathVariable themeId: String,
    ) = timetableThemeService.unsetDefault(user.id!!, themeId).let(::TimetableThemeDto)

    @DeleteMapping("/basic/{basicThemeTypeValue}/default")
    suspend fun unsetBasicThemeTypeDefault(
        @CurrentUser user: User,
        @PathVariable basicThemeTypeValue: Int,
    ): TimetableThemeDto {
        val basicThemeType =
            BasicThemeType.from(basicThemeTypeValue)
                ?: throw InvalidPathParameterException("basicThemeTypeValue")
        return timetableThemeService.unsetDefault(user.id!!, basicThemeType = basicThemeType).let(::TimetableThemeDto)
    }
}
