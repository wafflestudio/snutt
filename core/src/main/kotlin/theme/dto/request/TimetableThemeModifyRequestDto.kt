package com.wafflestudio.snutt.theme.dto.request

import com.wafflestudio.snutt.theme.data.ColorSet

data class TimetableThemeModifyRequestDto(
    val name: String?,
    val colors: List<ColorSet>?,
)
