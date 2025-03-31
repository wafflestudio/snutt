package com.wafflestudio.snutt.notification.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document(collection = "pushPreference")
data class PushPreference(
    @Id
    val id: String? = null,
    @Indexed(unique = true)
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    val pushPreferences: List<PushPreferenceItem>,
)
