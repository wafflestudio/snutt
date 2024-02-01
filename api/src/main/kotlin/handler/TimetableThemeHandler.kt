package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.enum.BasicThemeType
import com.wafflestudio.snu4t.common.exception.InvalidPathParameterException
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.dto.TimetableThemeDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableThemeAddRequestDto
import com.wafflestudio.snu4t.timetables.dto.request.TimetableThemeModifyRequestDto
import com.wafflestudio.snu4t.timetables.service.TimetableThemeService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class TimetableThemeHandler(
    private val timetableThemeService: TimetableThemeService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getThemes(req: ServerRequest) = handle(req) {
        val userId = req.userId

        timetableThemeService.getThemes(userId).map(::TimetableThemeDto)
    }

    suspend fun addTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val body = req.awaitBody<TimetableThemeAddRequestDto>()

        timetableThemeService.addTheme(userId, body.name, body.colors).let(::TimetableThemeDto)
    }

    suspend fun modifyTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")
        val body = req.awaitBody<TimetableThemeModifyRequestDto>()

        timetableThemeService.modifyTheme(userId, themeId, body.name, body.colors).let(::TimetableThemeDto)
    }

    suspend fun deleteTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.deleteTheme(userId, themeId)
    }

    suspend fun copyTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.copyTheme(userId, themeId).let(::TimetableThemeDto)
    }

    suspend fun setDefault(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.setDefault(userId, themeId).let(::TimetableThemeDto)
    }

    suspend fun setBasicThemeTypeDefault(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val basicThemeType = req.pathVariable("basicThemeTypeValue").toIntOrNull()?.let { BasicThemeType.from(it) } ?: throw InvalidPathParameterException("basicThemeTypeValue")

        timetableThemeService.setDefault(userId, basicThemeType = basicThemeType).let(::TimetableThemeDto)
    }

    suspend fun unsetDefault(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.unsetDefault(userId, themeId).let(::TimetableThemeDto)
    }

    suspend fun unsetBasicThemeTypeDefault(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val basicThemeType = req.pathVariable("basicThemeTypeValue").toIntOrNull()?.let { BasicThemeType.from(it) } ?: throw InvalidPathParameterException("basicThemeTypeValue")

        timetableThemeService.unsetDefault(userId, basicThemeType = basicThemeType).let(::TimetableThemeDto)
    }
}
