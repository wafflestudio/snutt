package com.wafflestudio.snutt.timetables.dto.request

import com.wafflestudio.snutt.common.enums.Semester

data class TimetableAddRequestDto(
    val year: Int,
    val semester: Semester,
    val title: String,
)
