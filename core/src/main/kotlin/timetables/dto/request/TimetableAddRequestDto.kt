package com.wafflestudio.snutt.timetables.dto.request

import com.wafflestudio.snutt.common.enum.Semester

data class TimetableAddRequestDto(
    val year: Int,
    val semester: Semester,
    val title: String,
)
