package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.timetables.dto.request.TimetableThemeRequest
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

        timetableThemeService.getThemes(userId)
    }

    suspend fun createTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val body = req.awaitBody<TimetableThemeRequest>()

        timetableThemeService.createTheme(userId, body.name, body.colors)
    }

    suspend fun copyTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.copyTheme(userId, themeId)
    }

    suspend fun deleteTheme(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.deleteTheme(userId, themeId)
    }

    suspend fun setDefault(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.setDefault(userId, themeId)
    }

    suspend fun unsetDefault(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val themeId = req.pathVariable("themeId")

        timetableThemeService.unsetDefault(userId, themeId)
    }
}
