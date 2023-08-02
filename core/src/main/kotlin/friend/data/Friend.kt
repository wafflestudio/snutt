package com.wafflestudio.snu4t.friend.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document
@CompoundIndex(def = "{ 'fromUserId': 1, 'toUserId': 1 }")
data class Friend(
    @Id
    val id: String? = null,
    @Field(targetType = FieldType.OBJECT_ID)
    val fromUserId: String,
    @Field(targetType = FieldType.OBJECT_ID)
    val toUserId: String,
    val isAccepted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
