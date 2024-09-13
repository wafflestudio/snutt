package com.wafflestudio.snu4t.theme.dto

import com.wafflestudio.snu4t.common.enum.BasicThemeType
import com.wafflestudio.snu4t.theme.data.ColorSet
import com.wafflestudio.snu4t.theme.data.ThemeOrigin
import com.wafflestudio.snu4t.theme.data.ThemeStatus
import com.wafflestudio.snu4t.theme.data.TimetableTheme
import com.wafflestudio.snu4t.theme.service.toBasicThemeType
import com.wafflestudio.snu4t.theme.service.toIdForTimetable

data class TimetableThemeDto(
    val id: String?,
    val theme: BasicThemeType,
    val name: String,
    val colors: List<ColorSet>?,
    val isDefault: Boolean,
    val isCustom: Boolean,
    var origin: ThemeOrigin? = null,
    var status: ThemeStatus = if (isCustom) ThemeStatus.PRIVATE else ThemeStatus.BASIC,
    var publishInfo: ThemeMarketInfoDto? = null,
)

data class ThemeMarketInfoDto(
    val publishName: String,
    val authorName: String,
    var downloads: Int,
)

fun TimetableThemeDto(timetableTheme: TimetableTheme) =
    with(timetableTheme) {
        TimetableThemeDto(
            id = toIdForTimetable(),
            theme = toBasicThemeType(),
            name = name,
            colors = colors,
            isDefault = isDefault,
            isCustom = isCustom,
            origin = origin,
            status = status,
            publishInfo = null,
        )
    }

fun TimetableThemeDto(timetableTheme: TimetableTheme, userNickname: String?) =
    userNickname?.let { nickname ->
        with(timetableTheme) {
            TimetableThemeDto(
                id = toIdForTimetable(),
                theme = toBasicThemeType(),
                name = name,
                colors = colors,
                isDefault = isDefault,
                isCustom = isCustom,
                origin = origin,
                status = status,
                publishInfo = publishInfo?.let {
                    ThemeMarketInfoDto(
                        publishName = it.publishName,
                        authorName = if (it.authorAnonymous) "익명" else nickname,
                        downloads = it.downloads,
                    )
                },
            )
        }
    }
