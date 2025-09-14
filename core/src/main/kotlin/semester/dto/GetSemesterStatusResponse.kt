package com.wafflestudio.snutt.semester.dto

import com.wafflestudio.snutt.semester.data.YearAndSemester

data class GetSemesterStatusResponse(
    val current: YearAndSemester?,
    val next: YearAndSemester,
)
