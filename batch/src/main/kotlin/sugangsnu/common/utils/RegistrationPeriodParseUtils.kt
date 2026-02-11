package com.wafflestudio.snutt.sugangsnu.common.utils

import com.wafflestudio.snutt.registrationperiod.data.RegistrationDate
import com.wafflestudio.snutt.registrationperiod.data.RegistrationPhase
import com.wafflestudio.snutt.registrationperiod.data.RegistrationTimeSlot
import org.jsoup.nodes.Element
import java.time.LocalDate

object RegistrationPeriodParseUtils {
    fun parseRegistrationDates(table: Element): Map<LocalDate, RegistrationDate> =
        table
            .select("tbody > tr")
            .mapNotNull { row ->
                val typeText = row.select("th[data-th=구분] span").text().trim()
                val phase = parseRegistrationPhase(typeText) ?: return@mapNotNull null
                val (startDate, endDate) = parseDateRange(row.select("td[data-th=일자]").text())

                (startDate.toEpochDay()..endDate.toEpochDay()).map { epochDay ->
                    LocalDate.ofEpochDay(epochDay) to
                        RegistrationDate(
                            date = LocalDate.ofEpochDay(epochDay),
                            vacantSeatRegistrationTimes = getVacantSeatRegistrationTimes(typeText),
                            phase = phase,
                        )
                }
            }.flatten()
            .toMap()

    private fun parseRegistrationPhase(typeText: String): RegistrationPhase? =
        when {
            typeText.contains("전산확정") -> null
            typeText.contains("정원외") -> null
            typeText.contains("수강취소") -> null
            typeText.contains("장바구니") -> null
            typeText.contains("신입생") && typeText.contains("선착순") -> RegistrationPhase.FRESHMAN
            typeText.contains("수강신청변경") -> RegistrationPhase.COURSE_CHANGE
            typeText.contains("선착순") -> RegistrationPhase.CURRENT_STUDENT
            else -> null
        }

    private fun getVacantSeatRegistrationTimes(typeText: String): List<RegistrationTimeSlot> =
        if (typeText.contains("수강신청변경")) {
            // 수강신청변경: 10~11시, 13~14시, 17~18시
            listOf(
                RegistrationTimeSlot(startMinute = 10 * 60, endMinute = 11 * 60),
                RegistrationTimeSlot(startMinute = 13 * 60, endMinute = 14 * 60),
                RegistrationTimeSlot(startMinute = 17 * 60, endMinute = 18 * 60),
            )
        } else {
            // 선착순수강신청: 10~11시, 13~14시, 15~16시
            listOf(
                RegistrationTimeSlot(startMinute = 10 * 60, endMinute = 11 * 60),
                RegistrationTimeSlot(startMinute = 13 * 60, endMinute = 14 * 60),
                RegistrationTimeSlot(startMinute = 15 * 60, endMinute = 16 * 60),
            )
        }

    private fun parseDateRange(dateText: String): Pair<LocalDate, LocalDate> {
        // Format: "2026-01-30(금) ~ 2026-01-30(금)"
        val dates = Regex("""(\d{4}-\d{2}-\d{2})""").findAll(dateText).map { it.value }.toList()
        val startDate = LocalDate.parse(dates[0])
        val endDate = LocalDate.parse(dates.getOrElse(1) { dates[0] })
        return startDate to endDate
    }
}
