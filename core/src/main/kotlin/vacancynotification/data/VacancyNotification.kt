package com.wafflestudio.snu4t.vacancynotification.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document("vacancy_notifications")
@CompoundIndex(def = "{ 'userId': 1, 'lectureId': 1 }", unique = true)
data class VacancyNotification(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val lectureId: String,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val coursebookId: String,
)
