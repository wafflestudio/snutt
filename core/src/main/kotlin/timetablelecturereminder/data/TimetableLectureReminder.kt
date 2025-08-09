package com.wafflestudio.snutt.timetablelecturereminder.data

import com.wafflestudio.snutt.common.enum.DayOfWeek
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant
import java.time.ZoneId

@Document
@CompoundIndex(def = "{'schedules.day': 1, 'schedules.minute': 1, 'schedules.recentNotifiedAt': 1}")
data class TimetableLectureReminder(
    @Id
    var id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    var timetableId: String,
    @Indexed(unique = true)
    @Field(targetType = FieldType.OBJECT_ID)
    var timetableLectureId: String,
    var offsetMinutes: Int,
    var schedules: List<Schedule>,
) {
    data class Schedule(
        val day: DayOfWeek,
        val minute: Int,
        val recentNotifiedAt: Instant? = null,
    ) : Comparable<Schedule> {
        companion object {
            fun fromInstant(
                instant: Instant,
                zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
            ): Schedule {
                val localDateTime = instant.atZone(zoneId).toLocalDateTime()
                val day = DayOfWeek.getOfValue(localDateTime.dayOfWeek.value - 1)!!
                val minute = localDateTime.hour * 60 + localDateTime.minute
                return Schedule(day, minute)
            }
        }

        fun plusMinutes(minutesToAdd: Int): Schedule {
            val totalMinutes = this.minute + minutesToAdd

            val minutesPerDay = 1440
            val daysToAdd = Math.floorDiv(totalMinutes, minutesPerDay)
            val newMinute = Math.floorMod(totalMinutes, minutesPerDay)

            val currentDayIndex = this.day.value
            val newDayIndex = Math.floorMod(currentDayIndex + daysToAdd, 7)
            val newDay = DayOfWeek.getOfValue(newDayIndex)!!

            return this.copy(day = newDay, minute = newMinute)
        }

        fun minusMinutes(minutesToSubtract: Int): Schedule = this.plusMinutes(-minutesToSubtract)

        fun isWithin(
            start: Schedule,
            end: Schedule,
        ): Boolean =
            if (start <= end) {
                this >= start && this <= end
            } else {
                this >= start || this <= end
            }

        override fun compareTo(other: Schedule): Int {
            val dayCompare = this.day.compareTo(other.day)
            if (dayCompare != 0) {
                return dayCompare
            }
            return this.minute.compareTo(other.minute)
        }
    }
}
