package com.wafflestudio.snutt.common.util

import com.wafflestudio.snutt.common.enum.Semester
import java.time.LocalDate
import java.time.MonthDay

object SemesterUtils {
    fun getCurrentYearAndSemester(): Pair<Int, Semester>? {
        val now = LocalDate.now()
        val currentMonthDay = MonthDay.from(now)
        return when {
            currentMonthDay <= MonthDay.of(1, 31) -> {
                now.year - 1 to Semester.WINTER
            }
            currentMonthDay in MonthDay.of(3, 2)..MonthDay.of(6, 23) -> {
                now.year to Semester.SPRING
            }
            currentMonthDay in MonthDay.of(6, 24)..MonthDay.of(8, 7) -> {
                now.year to Semester.SUMMER
            }
            currentMonthDay in MonthDay.of(9, 1)..MonthDay.of(12, 19) -> {
                now.year to Semester.AUTUMN
            }
            currentMonthDay >= MonthDay.of(12, 20) -> {
                now.year to Semester.WINTER
            }
            else -> {
                null // No semester matches the current date
            }
        }
    }
}
