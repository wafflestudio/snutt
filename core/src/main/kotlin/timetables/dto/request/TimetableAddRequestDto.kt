package com.wafflestudio.snu4t.timetables.dto.request

import com.wafflestudio.snu4t.common.enum.Semester

data class TimetableAddRequestDto(
    val year: Int,
    val semester: Semester,
    val title: String,
)
