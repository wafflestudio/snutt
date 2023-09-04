package com.wafflestudio.snu4t.friend.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document
@CompoundIndex(def = "{ 'fromUserId': 1, 'toUserId': 1 }")
data class Friend(
    @Id
    val id: String? = null,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val fromUserId: String,
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    val toUserId: String,
    var fromUserDisplayName: String? = null,
    var toUserDisplayName: String? = null,
    var isAccepted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun includes(userId: String): Boolean {
        return fromUserId == userId || toUserId == userId
    }

    fun getPartnerUserId(userId: String): String {
        check(this.includes(userId))
        return when (userId) {
            fromUserId -> toUserId
            toUserId -> fromUserId
            else -> throw IllegalArgumentException("UNREACHABLE")
        }
    }

    fun getPartnerDisplayName(userId: String): String? {
        check(this.includes(userId))
        return when (userId) {
            fromUserId -> toUserDisplayName
            toUserId -> fromUserDisplayName
            else -> throw IllegalArgumentException("UNREACHABLE")
        }
    }

    fun updatePartnerDisplayName(userId: String, displayName: String) {
        check(this.includes(userId))
        when (userId) {
            fromUserId -> toUserDisplayName = displayName
            toUserId -> fromUserDisplayName = displayName
            else -> throw IllegalArgumentException("UNREACHABLE")
        }

        updatedAt = LocalDateTime.now()
    }
}
