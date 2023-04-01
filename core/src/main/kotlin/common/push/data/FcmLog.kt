package com.wafflestudio.snu4t.common.push.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document("fcmlogs")
data class FcmLog(
    @Id
    @JsonProperty("_id")
    val id: String? = null,
    @Indexed(direction = IndexDirection.DESCENDING)
    @Field("date")
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val author: String,
    val to: String,
    val message: String,
    val cause: String,
    val response: String,
)