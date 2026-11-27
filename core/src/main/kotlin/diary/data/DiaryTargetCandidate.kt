package com.wafflestudio.snutt.diary.data

import com.wafflestudio.snutt.timetables.data.TimetableLecture
import java.time.LocalDate

data class DiaryTargetCandidate(
    val lecture: TimetableLecture,
    val heldToday: Boolean,
    val writable: Boolean,
    val lastClassDate: LocalDate?,
)
