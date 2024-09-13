package com.wafflestudio.snu4t.theme.dto.request

import com.wafflestudio.snu4t.theme.data.ColorSet

data class TimetableThemeModifyRequestDto(
    val name: String?,
    val colors: List<ColorSet>?,
)
