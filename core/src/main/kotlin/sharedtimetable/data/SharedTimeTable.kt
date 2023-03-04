package com.wafflestudio.snu4t.sharedtimetable.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.snu4t.timetables.data.TimeTable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document("shared_timetables")
data class SharedTimeTable(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    val userId: String,
    val title: String,
    val timetableId: String,
)
