package com.wafflestudio.snutt.mail.data

import com.wafflestudio.snutt.users.data.EMAIL_CASE_INSENSITIVE_COLLATION
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

enum class MailLogStatus {
    PENDING,
    SENT,
    SUPPRESSED,
    FAILED,
    THROTTLED,
    NO_ACCOUNT,
}

@Document
data class MailLog(
    @Id
    val id: String? = null,
    @Field
    val type: UserMailType,
    @Field
    val status: MailLogStatus,
    @Field
    @Indexed(collation = EMAIL_CASE_INSENSITIVE_COLLATION)
    val email: String,
    @Field
    val subject: String? = null,
    @Field
    val body: String? = null,
    @Field
    val errorMessage: String? = null,
    @Field
    @Indexed(expireAfter = "30d")
    val createdAt: Instant = Instant.now(),
    @Field
    val updatedAt: Instant = createdAt,
)
