package com.wafflestudio.snutt.common.util

import com.wafflestudio.snutt.common.enum.Semester
import java.time.Instant
import java.time.MonthDay
import java.time.ZoneId

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

    fun getCurrentYearAndSemester(now: Instant): Pair<Int, Semester>? {
        val localDate = now.atZone(ZoneId.of("Asia/Seoul")).toLocalDate()
        val currentMonthDay = MonthDay.from(localDate)
        val currentPeriod =
            semesterPeriods.firstOrNull { currentMonthDay in it.dateRange }
                ?: return null

        return localDate.year + currentPeriod.academicYearOffset to currentPeriod.semester
    }

    fun getCurrentOrNextYearAndSemester(now: Instant): Pair<Int, Semester> =
        getCurrentYearAndSemester(now) ?: run {
            val localDate = now.atZone(ZoneId.of("Asia/Seoul")).toLocalDate()
            val currentMonthDay = MonthDay.from(localDate)
            semesterPeriods.first { currentMonthDay < it.dateRange.start }.let { nextPeriod ->
                (localDate.year + nextPeriod.academicYearOffset) to nextPeriod.semester
            }
        }
}
