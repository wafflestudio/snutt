package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.exception.DuplicateThemeNameException
import com.wafflestudio.snu4t.common.exception.ThemeNotFoundException
import com.wafflestudio.snu4t.common.exception.TooManyThemeColorsException
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import com.wafflestudio.snu4t.timetables.repository.TimetableThemeRepository
import org.springframework.stereotype.Service

interface TimetableThemeService {
    suspend fun getThemes(userId: String): List<TimetableTheme>

    suspend fun createTheme(userId: String, name: String, colors: List<ColorSet>): TimetableTheme

    suspend fun copyTheme(userId: String, themeId: String): TimetableTheme

    suspend fun deleteTheme(userId: String, themeId: String)

    suspend fun setDefault(userId: String, themeId: String): TimetableTheme

    suspend fun unsetDefault(userId: String, themeId: String): TimetableTheme
}

@Service
class TimetableThemeServiceImpl(
    private val timetableThemeRepository: TimetableThemeRepository,
) : TimetableThemeService {
    companion object {
        private const val MAX_COLOR_COUNT = 9
        private val copyNumberRegex = """\s\(\d+\)$""".toRegex()
    }

    override suspend fun getThemes(userId: String): List<TimetableTheme> {
        return timetableThemeRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    override suspend fun createTheme(userId: String, name: String, colors: List<ColorSet>): TimetableTheme {
        if (colors.size > MAX_COLOR_COUNT) throw TooManyThemeColorsException
        if (timetableThemeRepository.existsByUserIdAndName(userId, name)) throw DuplicateThemeNameException

        val theme = TimetableTheme(
            userId = userId,
            name = name,
            colors = colors,
            isDefault = false,
        )
        return timetableThemeRepository.save(theme)
    }

    override suspend fun copyTheme(userId: String, themeId: String): TimetableTheme {
        val theme = timetableThemeRepository.findByIdAndUserId(themeId, userId) ?: throw ThemeNotFoundException

        val baseName = theme.name.replace(copyNumberRegex, "")
        val lastCopiedThemeNumber = getLastCopiedThemeNumber(userId, theme.name)

        val newTheme = TimetableTheme(
            userId = userId,
            name = "$baseName (${lastCopiedThemeNumber + 1})",
            colors = theme.colors,
            isDefault = false,
        )
        return timetableThemeRepository.save(newTheme)
    }

    private suspend fun getLastCopiedThemeNumber(userId: String, name: String): Int {
        val baseName = name.replace(copyNumberRegex, "")
        return timetableThemeRepository.findLastChild(userId, baseName)?.name
            ?.replace(baseName, "")?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    }

    override suspend fun deleteTheme(userId: String, themeId: String) {
        if (timetableThemeRepository.deleteByIdAndUserId(themeId, userId) == 0L) throw ThemeNotFoundException
    }

    override suspend fun setDefault(userId: String, themeId: String): TimetableTheme {
        val theme = timetableThemeRepository.findByIdAndUserId(themeId, userId) ?: throw ThemeNotFoundException

        val defaultThemeBefore = timetableThemeRepository.findByUserIdAndIsDefaultTrue(userId)
        defaultThemeBefore?.let {
            it.isDefault = false
            timetableThemeRepository.save(it)
        }

        theme.isDefault = true
        return timetableThemeRepository.save(theme)
    }

    override suspend fun unsetDefault(userId: String, themeId: String): TimetableTheme {
        val theme = timetableThemeRepository.findByIdAndUserId(themeId, userId) ?: throw ThemeNotFoundException

        theme.isDefault = false
        return timetableThemeRepository.save(theme)
    }

    suspend fun getDefaultTheme(userId: String): TimetableTheme? {
        return timetableThemeRepository.findByUserIdAndIsDefaultTrue(userId)
    }
}
