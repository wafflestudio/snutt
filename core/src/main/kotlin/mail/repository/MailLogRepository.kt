package com.wafflestudio.snutt.mail.repository

import com.wafflestudio.snutt.mail.data.MailLog
import com.wafflestudio.snutt.users.data.EMAIL_CASE_INSENSITIVE_COLLATION
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MailLogRepository : CoroutineCrudRepository<MailLog, String> {
    @Query(
        value = "{ 'email': ?0 }",
        sort = "{ 'createdAt': -1 }",
        collation = EMAIL_CASE_INSENSITIVE_COLLATION,
    )
    suspend fun findAllByEmail(email: String): List<MailLog>
}
