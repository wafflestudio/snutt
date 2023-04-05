package com.wafflestudio.snu4t.sharedtimetable.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.common.enum.Semester
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document("shared_timetables")
data class SharedTimetable(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Field("timetable_owner_id", targetType = FieldType.OBJECT_ID)
    val timetableOwnerId: String,
    var title: String,
    @Field("timetable_id")
    val timetableId: String,
    val year: Int,
    val semester: Semester,
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Field("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Field("is_deleted")
    var isDeleted: Boolean = false,
)
