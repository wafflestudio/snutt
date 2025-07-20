package com.wafflestudio.snutt.timetablelecturereminder.data

import com.wafflestudio.snutt.common.enum.DayOfWeek
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document
@CompoundIndex(def = "{'schedules.day': 1, 'schedules.minute': 1, 'schedules.notifiedAt': 1}")
data class TimetableLectureReminder(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    @Field(targetType = FieldType.OBJECT_ID)
    var timetableLectureId: String,
    var offsetMinutes: Int,
    var schedules: List<Schedule>,
) {
    data class Schedule(
        val day: DayOfWeek,
        val minute: Int,
        val notifiedAt: Instant? = null,
    ) {
        operator fun plus(minutesToAdd: Int): Schedule {
            val totalMinutes = this.minute + minutesToAdd
            val daysToAdd = totalMinutes / 1440
            val newMinute = totalMinutes % 1440

            val currentDayIndex = this.day.value
            val newDayIndex = (currentDayIndex + daysToAdd) % 7
            val newDay = DayOfWeek.getOfValue(((newDayIndex % 7) + 7) % 7)!!

            return this.copy(day = newDay, minute = newMinute)
        }

        operator fun minus(minutesToSubtract: Int): Schedule {
            return this.plus(-minutesToSubtract)
        }
    }
}
