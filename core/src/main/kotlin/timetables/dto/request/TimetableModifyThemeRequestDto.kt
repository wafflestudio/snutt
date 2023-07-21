package com.wafflestudio.snu4t.timetables.dto.request

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.common.enum.TimetableTheme

data class TimetableModifyThemeRequestDto (
    val theme: TimetableTheme,
)