package com.wafflestudio.snu4t.theme.dto.request

import com.wafflestudio.snu4t.common.enum.BasicThemeType

data class TimetableModifyThemeRequestDto(
    val theme: BasicThemeType?,
    val themeId: String?,
)
