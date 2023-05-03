package com.wafflestudio.snu4t.notification.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document("notifications")
@CompoundIndex(def = "{ 'user_id': 1, 'created_at': -1 }")
data class Notification(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed
    @Field("user_id", targetType = FieldType.OBJECT_ID)
    @JsonProperty("user_id")
    val userId: String?,
    val message: String,
    val type: NotificationType,
    // link: 사용하지 않음
    // https://github.com/wafflestudio/snutt-timetable/pull/45#discussion_r1155538953 참고
    private val link: String? = null,
    @Indexed(direction = IndexDirection.DESCENDING)
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
