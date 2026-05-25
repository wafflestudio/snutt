package com.wafflestudio.snutt.mail.service

import com.wafflestudio.snutt.common.exception.InvalidEmailException
import com.wafflestudio.snutt.common.mail.MailClient
import com.wafflestudio.snutt.mail.data.MailContent
import com.wafflestudio.snutt.mail.data.UserMailType
import com.wafflestudio.snutt.users.service.AuthService
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

interface MailService {
    suspend fun sendUserMail(
        type: UserMailType,
        to: String,
        code: String = "",
        localId: String = "",
        accountInfo: String = "",
    )
}

@Service
class MailServiceImpl(
    private val resourceLoader: ResourceLoader,
    private val mailClient: MailClient,
    private val authService: AuthService,
) : MailService {
    private val mailTemplateResource by lazy {
        resourceLoader.getResource("classpath:userMailTemplate.txt")
    }

    override suspend fun sendUserMail(
        type: UserMailType,
        to: String,
        code: String,
        localId: String,
        accountInfo: String,
    ) {
        if (!authService.isValidEmail(to)) {
            throw InvalidEmailException
        }
        getUserMailContent(type, to, code, localId, accountInfo).let { (subject, body) ->
            mailClient.sendMail(to, subject, body)
        }
    }

    private suspend fun getUserMailContent(
        type: UserMailType,
        email: String,
        code: String,
        localId: String,
        accountInfo: String,
    ): MailContent {
        val mailContentList =
            mailTemplateResource.inputStream
                .readBytes()
                .decodeToString()
                .replace("{code}", code)
                .replace("{email}", email)
                .replace("{localId}", localId)
                .replace("{accountInfo}", accountInfo)
                .split("\n\n")
                .windowed(2, 2, false)
                .map { (subject, body) -> MailContent(subject, body) }
        val mailContent = mailContentList[type.index]
        return mailContent
    }
}
