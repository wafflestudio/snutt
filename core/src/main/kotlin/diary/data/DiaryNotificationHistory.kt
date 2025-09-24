package com.wafflestudio.snutt.diary.data

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant

@Document
data class DiaryNotificationHistory(
    @Field(targetType = FieldType.OBJECT_ID)
    val userId: String,
    val recentNotifiedAt: Instant? = null,
)
