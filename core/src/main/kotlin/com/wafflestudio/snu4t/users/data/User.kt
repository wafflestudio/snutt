package com.wafflestudio.snu4t.users.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("users")
class User(
        @Id
        var id: String? = null,
        var active: Boolean,
        var credential: Credential,
        var email: String?,
        var isAdmin: Boolean,
        var notificationCheckedAt: LocalDateTime,
        var regDate: LocalDateTime,
        var fcmKey: String?,
        var isEmailVerified: Boolean?,
        var lastLoginTimestamp: Long,
        var credentialHash: String,
)