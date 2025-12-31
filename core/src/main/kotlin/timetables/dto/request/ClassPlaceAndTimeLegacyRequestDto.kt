package com.wafflestudio.snutt.timetables.dto.request

import com.wafflestudio.snutt.common.enums.DayOfWeek
import com.wafflestudio.snutt.common.exception.InvalidTimeException
import com.wafflestudio.snutt.lectures.data.ClassPlaceAndTime

data class ClassPlaceAndTimeLegacyRequestDto(
    val day: DayOfWeek,
    val place: String?,
    val startMinute: Int?,
    val endMinute: Int?,
) {
    fun toClassPlaceAndTime(): ClassPlaceAndTime {
        val startMinute =
            this.startMinute ?: throw InvalidTimeException
        val endMinute =
            this.endMinute ?: throw InvalidTimeException
        // 23:55 이후에 끝나는 수업
        if (endMinute > 23 * 60 + 55) throw InvalidTimeException
        // 5분 미만 수업
        if (endMinute - startMinute < 5) throw InvalidTimeException

        return ClassPlaceAndTime(
            day = day,
            place = place ?: "",
            startMinute = startMinute,
            endMinute = endMinute,
        )
    }
}
