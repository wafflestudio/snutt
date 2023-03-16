package com.wafflestudio.snu4t.sharedtimetable.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document("shared_timetables")
data class SharedTimeTable(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    val userId: String,
    var title: String,
    val timetableId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var isDeleted: Boolean = false,
)
