package com.wafflestudio.snu4t.timetables.dto

import com.wafflestudio.snu4t.common.enum.BasicThemeType
import com.wafflestudio.snu4t.timetables.data.ColorSet
import com.wafflestudio.snu4t.timetables.data.TimetableTheme
import com.wafflestudio.snu4t.timetables.service.toBasicThemeType

data class TimetableThemeDto(
    val id: String?,
    val theme: BasicThemeType,
    val name: String,
    val colors: List<ColorSet>?,
    val isDefault: Boolean,
    val isCustom: Boolean,
)

fun TimetableThemeDto(timetableTheme: TimetableTheme) =
    with(timetableTheme) {
        TimetableThemeDto(
            id = id,
            theme = toBasicThemeType(),
            name = name,
            colors = colors,
            isDefault = isDefault,
            isCustom = isCustom,
        )
    }
