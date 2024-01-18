package com.wafflestudio.snu4t.timetables.dto.request

import com.wafflestudio.snu4t.timetables.data.ColorSet

data class TimetableThemeModifyRequestDto(
    val name: String?,
    val colors: List<ColorSet>?,
)
