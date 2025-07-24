package com.wafflestudio.snutt.common.util

import com.wafflestudio.snutt.common.enum.Semester
import java.time.LocalDate
import java.time.MonthDay

object SemesterUtils {
    private val dateToSemester = mapOf(
        MonthDay.of(1, 1)..MonthDay.of(1, 31) to (Semester.WINTER to -1),
        MonthDay.of(3, 2)..MonthDay.of(6, 23) to (Semester.SPRING to 0),
        MonthDay.of(6, 24)..MonthDay.of(8, 7) to (Semester.SUMMER to 0),
        MonthDay.of(9, 1)..MonthDay.of(12, 19) to (Semester.AUTUMN to 0),
        MonthDay.of(12, 20)..MonthDay.of(12, 31) to (Semester.WINTER to 0)
    )

    fun getCurrentYearAndSemester(): Pair<Int, Semester>? {
        val now = LocalDate.now()
        val currentMonthDay = MonthDay.from(now)

        val (semester, yearOffset) = dateToSemester.entries.firstOrNull { currentMonthDay in it.key }?.value
            ?: return null

        return (now.year + yearOffset) to semester
    }

    fun getCurrentOrNextYearAndSemester(): Pair<Int, Semester> {
        val now = LocalDate.now()
        val currentMonthDay = MonthDay.from(now)

        return getCurrentYearAndSemester()
            ?: dateToSemester.entries.firstOrNull { currentMonthDay < it.key.start }!!.value.let { (semester, yearOffset) ->
                (now.year + yearOffset) to semester
            }
    }
}
