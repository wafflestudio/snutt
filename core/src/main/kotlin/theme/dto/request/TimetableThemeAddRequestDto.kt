package com.wafflestudio.snu4t.theme.dto.request

import com.wafflestudio.snu4t.theme.data.ColorSet

data class TimetableThemeAddRequestDto(
    val name: String,
    val colors: List<ColorSet>,
)
