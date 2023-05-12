package com.wafflestudio.snu4t.seatsnotification.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document("seat_notification")
data class SeatNotification(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val lectureId: String,
)
