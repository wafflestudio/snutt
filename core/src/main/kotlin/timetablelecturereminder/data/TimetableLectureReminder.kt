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
        var day: DayOfWeek,
        var minute: Int,
        var notifiedAt: Instant? = null,
    )
}
