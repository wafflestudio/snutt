package com.wafflestudio.snu4t.timetables.dto.request

import com.wafflestudio.snu4t.timetables.data.ColorSet

data class TimetableThemeAddRequestDto(
    val name: String,
    val colors: List<ColorSet>,
)
