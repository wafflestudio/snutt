package com.wafflestudio.snu4t.mail.utils

import com.wafflestudio.snu4t.common.exception.InvalidEmailException
import com.wafflestudio.snu4t.mail.MailClient
import com.wafflestudio.snu4t.users.service.AuthService
import org.springframework.stereotype.Service

interface MailService {
    suspend fun sendMail(to: String, subject: String, body: String)
}

@Service
class MailServiceImpl(
    private val mailClient: MailClient,
    private val authService: AuthService,
) : MailService {
    override suspend fun sendMail(to: String, subject: String, body: String) {
        if (!authService.isValidEmail(to)) {
            throw InvalidEmailException
        }
        mailClient.sendMail(to, subject, body)
    }
}
