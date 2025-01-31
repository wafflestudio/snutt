package com.wafflestudio.snutt.theme.dto.request

import com.wafflestudio.snutt.common.enum.BasicThemeType

data class TimetableModifyThemeRequestDto(
    val theme: BasicThemeType?,
    val themeId: String?,
)
