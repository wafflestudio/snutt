package com.wafflestudio.snutt.theme.dto.request

import com.wafflestudio.snutt.theme.data.ColorSet

data class TimetableThemeAddRequestDto(
    val name: String,
    val colors: List<ColorSet>,
)
