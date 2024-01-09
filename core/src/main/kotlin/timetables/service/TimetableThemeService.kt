package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.enum.BasicThemeType
import com.wafflestudio.snu4t.common.exception.DuplicateThemeNameException
import com.wafflestudio.snu4t.common.exception.InvalidThemeColorCountException
import com.wafflestudio.snu4t.common.exception.InvalidThemeTypeException
import com.wafflestudio.snu4t.common.exception.ThemeNotFoundException
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import com.wafflestudio.snu4t.timetables.repository.TimetableThemeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface TimetableThemeService {
    suspend fun getThemes(userId: String): List<TimetableTheme>

    suspend fun createTheme(userId: String, name: String, colors: List<ColorSet>): TimetableTheme

    suspend fun modifyTheme(userId: String, themeId: String, name: String?, colors: List<ColorSet>?): TimetableTheme

    suspend fun deleteTheme(userId: String, themeId: String)

    suspend fun copyTheme(userId: String, themeId: String): TimetableTheme

    suspend fun setDefault(userId: String, themeId: String? = null, basicThemeType: BasicThemeType? = null): TimetableTheme

    suspend fun unsetDefault(userId: String, themeId: String)

    suspend fun getDefaultTheme(userId: String): TimetableTheme?

    suspend fun getTheme(userId: String, themeId: String?, basicThemeType: BasicThemeType?): TimetableTheme
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
        val customThemes = timetableThemeRepository.findByUserIdAndIsCustomTrueOrderByCreatedAtDesc(userId)
        val defaultTheme = getDefaultTheme(userId)

        return (
            BasicThemeType.values().map { buildTimetableTheme(userId, it, isDefault = it.displayName == defaultTheme?.name) } +
                customThemes
            )
    }

    override suspend fun createTheme(userId: String, name: String, colors: List<ColorSet>): TimetableTheme {
        if (colors.size !in 1..MAX_COLOR_COUNT) throw InvalidThemeColorCountException
        if (timetableThemeRepository.existsByUserIdAndName(userId, name)) throw DuplicateThemeNameException

        val theme = TimetableTheme(
            userId = userId,
            name = name,
            colors = colors,
            isCustom = true,
            isDefault = false,
        )
        return timetableThemeRepository.save(theme)
    }

    override suspend fun modifyTheme(userId: String, themeId: String, name: String?, colors: List<ColorSet>?): TimetableTheme {
        val theme = getCustomTheme(userId, themeId)

        name?.let {
            if (theme.name != it && timetableThemeRepository.existsByUserIdAndName(userId, it)) throw DuplicateThemeNameException
            theme.name = it
        }
        colors?.let { theme.colors = it }
        theme.updatedAt = LocalDateTime.now()
        return timetableThemeRepository.save(theme)
    }

    override suspend fun deleteTheme(userId: String, themeId: String) {
        val theme = getCustomTheme(userId, themeId)
        timetableThemeRepository.delete(theme)
    }

    override suspend fun copyTheme(userId: String, themeId: String): TimetableTheme {
        val theme = getCustomTheme(userId, themeId)

        val baseName = theme.name.replace(copyNumberRegex, "")
        val lastCopiedThemeNumber = getLastCopiedThemeNumber(userId, theme.name)

        val newTheme = TimetableTheme(
            userId = userId,
            name = "$baseName (${lastCopiedThemeNumber + 1})",
            colors = theme.colors,
            isCustom = true,
            isDefault = false,
        )
        return timetableThemeRepository.save(newTheme)
    }

    private suspend fun getLastCopiedThemeNumber(userId: String, name: String): Int {
        val baseName = name.replace(copyNumberRegex, "")
        return timetableThemeRepository.findLastChild(userId, baseName)?.name
            ?.replace(baseName, "")?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    }

    override suspend fun setDefault(userId: String, themeId: String?, basicThemeType: BasicThemeType?): TimetableTheme {
        require((themeId == null) xor (basicThemeType == null))

        val theme = themeId?.let {
            timetableThemeRepository.findByIdAndUserId(it, userId) ?: throw ThemeNotFoundException
        } ?: buildTimetableTheme(userId, basicThemeType!!, isDefault = true)

        val defaultThemeBefore = timetableThemeRepository.findByUserIdAndIsDefaultTrue(userId)
        defaultThemeBefore?.let {
            if (it.isCustom) {
                it.isDefault = false
                it.updatedAt = LocalDateTime.now()
                timetableThemeRepository.save(it)
            } else {
                timetableThemeRepository.delete(it)
            }
        }

        theme.isDefault = true
        theme.updatedAt = LocalDateTime.now()
        return timetableThemeRepository.save(theme)
    }

    override suspend fun unsetDefault(userId: String, themeId: String) {
        val theme = timetableThemeRepository.findByIdAndUserId(themeId, userId) ?: throw ThemeNotFoundException
        if (!theme.isDefault) return

        if (theme.isCustom) {
            theme.isDefault = false
            theme.updatedAt = LocalDateTime.now()
            timetableThemeRepository.save(theme)
        } else {
            timetableThemeRepository.delete(theme)
        }

        timetableThemeRepository.save(buildTimetableTheme(userId, BasicThemeType.SNUTT, isDefault = true))
    }

    override suspend fun getDefaultTheme(userId: String): TimetableTheme? {
        return timetableThemeRepository.findByUserIdAndIsDefaultTrue(userId)
    }

    override suspend fun getTheme(userId: String, themeId: String?, basicThemeType: BasicThemeType?): TimetableTheme {
        require((themeId == null) xor (basicThemeType == null))

        val defaultTheme = getDefaultTheme(userId)

        return themeId?.let {
            timetableThemeRepository.findByIdAndUserId(it, userId) ?: throw ThemeNotFoundException
        } ?: buildTimetableTheme(userId, basicThemeType!!, isDefault = basicThemeType.displayName == defaultTheme?.name)
    }

    private suspend fun getCustomTheme(userId: String, themeId: String): TimetableTheme {
        return timetableThemeRepository.findByIdAndUserId(themeId, userId)?.also {
            if (!it.isCustom) throw InvalidThemeTypeException
        } ?: throw ThemeNotFoundException
    }

    private fun buildTimetableTheme(userId: String, basicThemeType: BasicThemeType, isDefault: Boolean) =
        TimetableTheme(
            userId = userId,
            name = basicThemeType.displayName,
            colors = null,
            isCustom = false,
            isDefault = isDefault,
        )
}

fun TimetableTheme?.toBasicThemeType() = if (this == null || isCustom) BasicThemeType.SNUTT else requireNotNull(BasicThemeType.from(name))
