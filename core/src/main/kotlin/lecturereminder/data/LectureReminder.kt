package com.wafflestudio.snutt.lecturereminder.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

data class LectureReminder(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val timetableLectureId: String,
    val offset: LectureReminderOffset,
)

enum class LectureReminderOffset(
    minutes: Int,
) {
    ZERO(0),
    TEN_MINUTES_BEFORE(-10),
    TEM_MINUTES_AFTER(10),
}
