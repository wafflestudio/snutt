package com.wafflestudio.snutt.lecturenotification.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document
@CompoundIndexes(
    CompoundIndex(name = "minuteOfWeek_idx", def = "{'lectureStartTimes.minuteOfWeek': 1}"),
)
data class LectureNotification(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val timetableId: String,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val lectureId: String,
    val type: LectureNotificationType,
    val schedules: List<LectureNotificationSchedule>,
)

data class LectureNotificationSchedule(
    val minuteOfWeek: Int,
    val notifiedAt: Instant? = null,
)
