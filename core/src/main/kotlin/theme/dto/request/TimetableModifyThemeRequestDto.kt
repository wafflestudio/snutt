package com.wafflestudio.snutt.theme.dto.request

import com.wafflestudio.snutt.common.enums.BasicThemeType

data class TimetableModifyThemeRequestDto(
    val theme: BasicThemeType?,
    val themeId: String?,
)
