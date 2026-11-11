package com.wafflestudio.snutt.mail.dto

import com.wafflestudio.snutt.mail.data.MailLog
import com.wafflestudio.snutt.mail.data.MailLogStatus
import com.wafflestudio.snutt.mail.data.UserMailType

data class AdminMailLogResponse(
    val id: String,
    val type: UserMailType,
    val status: MailLogStatus,
    val email: String?,
    val subject: String?,
    val body: String?,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        fun from(mailLog: MailLog): AdminMailLogResponse =
            AdminMailLogResponse(
                id = mailLog.id!!,
                type = mailLog.type,
                status = mailLog.status,
                email = mailLog.email,
                subject = mailLog.subject,
                body = mailLog.body,
                errorMessage = mailLog.errorMessage,
                createdAt = mailLog.createdAt.toEpochMilli(),
                updatedAt = mailLog.updatedAt.toEpochMilli(),
            )
    }
}
