package com.wafflestudio.snutt.semester.service

import com.wafflestudio.snutt.common.enum.Semester
import com.wafflestudio.snutt.semester.data.YearAndSemester
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface SemesterService {
    fun getCurrentYearAndSemester(currentTime: Instant): YearAndSemester?

    fun getNextYearAndSemester(currentTime: Instant): YearAndSemester
}

@Service
class SemesterServiceImpl : SemesterService {
    private data class SemesterWithDateRange(
        val semester: Semester,
        val year: Int,
        val startDate: LocalDate,
        val endDate: LocalDate,
    )

    private fun generateSemesterSequence(fromYear: Int): Sequence<SemesterWithDateRange> =
        generateSequence(fromYear) { it + 1 }
            .flatMap { year ->
                sequenceOf(
                    SemesterWithDateRange(
                        semester = Semester.SPRING,
                        year = year,
                        startDate = LocalDate.of(year, 3, 2),
                        endDate = LocalDate.of(year, 6, 23),
                    ),
                    SemesterWithDateRange(
                        semester = Semester.SUMMER,
                        year = year,
                        startDate = LocalDate.of(year, 6, 24),
                        endDate = LocalDate.of(year, 8, 7),
                    ),
                    SemesterWithDateRange(
                        semester = Semester.AUTUMN,
                        year = year,
                        startDate = LocalDate.of(year, 9, 1),
                        endDate = LocalDate.of(year, 10, 31),
                    ),
                    SemesterWithDateRange(
                        semester = Semester.WINTER,
                        year = year,
                        startDate = LocalDate.of(year, 11, 2),
                        endDate = LocalDate.of(year + 1, 1, 31),
                    ),
                )
            }

    override fun getCurrentYearAndSemester(currentTime: Instant): YearAndSemester? {
        val currentDate = currentTime.atZone(KST).toLocalDate()
        return generateSemesterSequence(currentDate.year - 1)
            .takeWhile { currentDate >= it.startDate }
            .firstOrNull { currentDate in it.startDate..it.endDate }
            ?.let { YearAndSemester(year = it.year, semester = it.semester) }
    }

    override fun getNextYearAndSemester(currentTime: Instant): YearAndSemester {
        val currentDate = currentTime.atZone(KST).toLocalDate()
        return generateSemesterSequence(currentDate.year)
            .first { currentDate < it.startDate }
            .let { YearAndSemester(year = it.year, semester = it.semester) }
    }

    companion object {
        private val KST = ZoneId.of("Asia/Seoul")
    }
}
