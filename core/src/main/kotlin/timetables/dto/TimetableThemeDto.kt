package com.wafflestudio.snu4t.timetables.dto

import com.wafflestudio.snu4t.common.enum.BasicThemeType
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.ThemeMarketInfo
import com.wafflestudio.snu4t.timetables.data.ThemeOrigin
import com.wafflestudio.snu4t.timetables.data.ThemeStatus
import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import com.wafflestudio.snu4t.timetables.service.toBasicThemeType
import com.wafflestudio.snu4t.timetables.service.toIdForTimetable

data class TimetableThemeDto(
    val id: String?,
    val theme: BasicThemeType,
    val name: String,
    val colors: List<ColorSet>?,
    val isDefault: Boolean,
    val isCustom: Boolean,
    var origin: ThemeOrigin? = null,
    var status: ThemeStatus = if(isCustom) ThemeStatus.PRIVATE else ThemeStatus.BASIC,
    var publishInfo: ThemeMarketInfo? = null,
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
            publishInfo = publishInfo,
        )
    }
