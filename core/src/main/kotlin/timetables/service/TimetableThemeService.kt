package com.wafflestudio.snu4t.timetables.service

import com.wafflestudio.snu4t.common.enum.BasicThemeType
import com.wafflestudio.snu4t.common.exception.DefaultThemeDeleteErrorException
import com.wafflestudio.snu4t.common.exception.DuplicateThemeNameException
import com.wafflestudio.snu4t.common.exception.InvalidThemeColorCountException
import com.wafflestudio.snu4t.common.exception.InvalidThemeTypeException
import com.wafflestudio.snu4t.common.exception.NotDefaultThemeErrorException
import com.wafflestudio.snu4t.common.exception.ThemeNotFoundException
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.Timetable
import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import com.wafflestudio.snu4t.timetables.repository.TimetableRepository
import com.wafflestudio.snu4t.timetables.repository.TimetableThemeRepository
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface TimetableThemeService {
    suspend fun getThemes(userId: String): List<TimetableTheme>

    suspend fun addTheme(userId: String, name: String, colors: List<ColorSet>): TimetableTheme

    suspend fun modifyTheme(userId: String, themeId: String, name: String?, colors: List<ColorSet>?): TimetableTheme

    suspend fun deleteTheme(userId: String, themeId: String)

    suspend fun copyTheme(userId: String, themeId: String): TimetableTheme

    suspend fun setDefault(userId: String, themeId: String? = null, basicThemeType: BasicThemeType? = null): TimetableTheme

    suspend fun unsetDefault(userId: String, themeId: String? = null, basicThemeType: BasicThemeType? = null): TimetableTheme

    suspend fun getDefaultTheme(userId: String): TimetableTheme?

    suspend fun getTheme(userId: String, themeId: String? = null, basicThemeType: BasicThemeType? = null): TimetableTheme

    suspend fun getNewColorIndexAndColor(timetable: Timetable): Pair<Int, ColorSet?>
}

@Service
class TimetableThemeServiceImpl(
    private val timetableThemeRepository: TimetableThemeRepository,
    private val timetableRepository: TimetableRepository,
) : TimetableThemeService {
    companion object {
        private const val MAX_COLOR_COUNT = 9
        private val copyNumberRegex = """\s\(\d+\)$""".toRegex()
    }

    override suspend fun getThemes(userId: String): List<TimetableTheme> {
        val customThemes = timetableThemeRepository.findByUserIdAndIsCustomTrueOrderByCreatedAtDesc(userId)
        val defaultTheme = getDefaultTheme(userId)

        val basicThemes = BasicThemeType.values().map {
            buildTimetableTheme(
                userId,
                it,
                isDefault = (
                    it.displayName == defaultTheme?.name ||
                        (defaultTheme == null && it == BasicThemeType.SNUTT)
                    )
            )
        }

        return basicThemes + customThemes
    }

    override suspend fun addTheme(userId: String, name: String, colors: List<ColorSet>): TimetableTheme {
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

        colors?.let {
            if (it.size !in 1..MAX_COLOR_COUNT) throw InvalidThemeColorCountException

            val colorMap = requireNotNull(theme.colors).mapIndexed { i, color -> color to colors.getOrNull(i) }.toMap()

            val timetables = timetableRepository.findByUserIdAndThemeId(userId, themeId)
            timetables.forEach { timetable ->
                timetable.lectures.forEach { lecture ->
                    if (lecture.color in theme.colors!!) {
                        colorMap[lecture.color]?.let { newColor -> lecture.color = newColor }
                    }
                }
            }
            timetableRepository.saveAll(timetables).collect()

            theme.colors = it
        }
        theme.updatedAt = LocalDateTime.now()
        return timetableThemeRepository.save(theme)
    }

    override suspend fun deleteTheme(userId: String, themeId: String) {
        val theme = getCustomTheme(userId, themeId)
        if (theme.isDefault) throw DefaultThemeDeleteErrorException

        val timetables = timetableRepository.findByUserIdAndThemeId(userId, themeId)

        timetables.forEach {
            it.theme = BasicThemeType.SNUTT
            it.themeId = null
        }
        timetableRepository.saveAll(timetables).collect()

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

    override suspend fun unsetDefault(userId: String, themeId: String?, basicThemeType: BasicThemeType?): TimetableTheme {
        require((themeId == null) xor (basicThemeType == null))

        val theme = getDefaultTheme(userId) ?: throw NotDefaultThemeErrorException

        themeId?.let {
            if (theme.isCustom.not() || theme.id != it) throw NotDefaultThemeErrorException
        } ?: run {
            if (theme.isCustom || theme.name != basicThemeType!!.displayName) throw NotDefaultThemeErrorException
        }

        if (theme.isCustom) {
            theme.isDefault = false
            theme.updatedAt = LocalDateTime.now()
            timetableThemeRepository.save(theme)
        } else {
            timetableThemeRepository.delete(theme)
        }

        return timetableThemeRepository.save(buildTimetableTheme(userId, BasicThemeType.SNUTT, isDefault = true))
    }

    override suspend fun getDefaultTheme(userId: String): TimetableTheme? {
        return timetableThemeRepository.findByUserIdAndIsDefaultTrue(userId)
    }

    override suspend fun getTheme(userId: String, themeId: String?, basicThemeType: BasicThemeType?): TimetableTheme {
        require((themeId == null) xor (basicThemeType == null))

        val defaultTheme = getDefaultTheme(userId)

        return themeId?.let {
            timetableThemeRepository.findByIdAndUserId(it, userId) ?: throw ThemeNotFoundException
        } ?: buildTimetableTheme(
            userId,
            basicThemeType!!,
            isDefault = (
                basicThemeType.displayName == defaultTheme?.name ||
                    (defaultTheme == null && basicThemeType == BasicThemeType.SNUTT)
                ),
        )
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

    override suspend fun getNewColorIndexAndColor(timetable: Timetable): Pair<Int, ColorSet?> {
        return if (timetable.themeId == null) {
            val alreadyUsedIndexes = timetable.lectures.map { it.colorIndex }
            val indexToCount = (1..MAX_COLOR_COUNT).associateWith { colorIndex -> alreadyUsedIndexes.count { it == colorIndex } }

            val minCount = indexToCount.minOf { it.value }
            indexToCount.entries.filter { (_, count) -> count == minCount }.map { it.key }.random() to null
        } else {
            val theme = getTheme(timetable.userId, timetable.themeId)

            val alreadyUsedColors = timetable.lectures.map { requireNotNull(it.color) }
            val colors = requireNotNull(theme.colors) { "theme.colors 가 null - userId: ${timetable.userId}, timetableId: ${timetable.id}, themeId: ${theme.id}" }
            val colorToCount = colors.associateWith { color -> alreadyUsedColors.count { it == color } }

            val minCount = colorToCount.minOf { it.value }
            0 to colorToCount.entries.filter { (_, count) -> count == minCount }.map { it.key }.random()
        }
    }
}

fun TimetableTheme?.toBasicThemeType() = if (this == null || isCustom) BasicThemeType.SNUTT else requireNotNull(BasicThemeType.from(name))
