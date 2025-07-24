package com.wafflestudio.snutt.common.util

import com.wafflestudio.snutt.common.enum.Semester
import java.time.LocalDate
import java.time.MonthDay

object SemesterUtils {
    private data class SemesterInfo(
        val semester: Semester,
        val academicYearOffset: Int,
        val dateRange: ClosedRange<MonthDay>,
    )

    private val semesterPeriods =
        listOf(
            SemesterInfo(Semester.WINTER, -1, MonthDay.of(1, 1)..MonthDay.of(1, 31)),
            SemesterInfo(Semester.SPRING, 0, MonthDay.of(3, 2)..MonthDay.of(6, 23)),
            SemesterInfo(Semester.SUMMER, 0, MonthDay.of(6, 24)..MonthDay.of(8, 7)),
            SemesterInfo(Semester.AUTUMN, 0, MonthDay.of(9, 1)..MonthDay.of(12, 19)),
            SemesterInfo(Semester.WINTER, 0, MonthDay.of(12, 20)..MonthDay.of(12, 31)),
        )

    fun getCurrentYearAndSemester(): Pair<Int, Semester>? {
        val now = LocalDate.now()
        val currentMonthDay = MonthDay.from(now)
        val currentPeriod =
            semesterPeriods.firstOrNull { currentMonthDay in it.dateRange }
                ?: return null

        return now.year + currentPeriod.academicYearOffset to currentPeriod.semester
    }

    fun getCurrentOrNextYearAndSemester(): Pair<Int, Semester> {
        return getCurrentYearAndSemester() ?: run {
            val now = LocalDate.now()
            val currentMonthDay = MonthDay.from(now)
            semesterPeriods.first { currentMonthDay < it.dateRange.start }.let { nextPeriod ->
                (now.year + nextPeriod.academicYearOffset) to nextPeriod.semester
            }
        }
    }
}
