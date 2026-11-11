package com.wafflestudio.snutt.mail.service

import com.wafflestudio.snutt.common.exception.InvalidEmailException
import com.wafflestudio.snutt.common.exception.TooManyVerificationCodeRequestException
import com.wafflestudio.snutt.common.mail.MailClient
import com.wafflestudio.snutt.common.mail.MailSendOutcome
import com.wafflestudio.snutt.mail.data.MailContent
import com.wafflestudio.snutt.mail.data.MailLog
import com.wafflestudio.snutt.mail.data.MailLogStatus
import com.wafflestudio.snutt.mail.data.UserMailType
import com.wafflestudio.snutt.mail.repository.MailLogRepository
import com.wafflestudio.snutt.users.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.time.Instant

interface MailService {
    suspend fun sendUserMail(
        type: UserMailType,
        to: String,
        code: String = "",
        localId: String = "",
        accountInfo: String = "",
        prepare: suspend () -> Unit = {},
    )

    suspend fun recordMailLog(
        type: UserMailType,
        status: MailLogStatus,
        email: String,
        error: Throwable? = null,
    )

    suspend fun getMailLogs(email: String): List<MailLog>
}

@Service
class MailServiceImpl(
    private val resourceLoader: ResourceLoader,
    private val mailClient: MailClient,
    private val authService: AuthService,
    private val mailLogRepository: MailLogRepository,
) : MailService {
    private val log = LoggerFactory.getLogger(javaClass)

    private val mailTemplateResource by lazy {
        resourceLoader.getResource("classpath:userMailTemplate.txt")
    }

    override suspend fun sendUserMail(
        type: UserMailType,
        to: String,
        code: String,
        localId: String,
        accountInfo: String,
        prepare: suspend () -> Unit,
    ) {
        val (subject, body) =
            try {
                prepare()
                if (!authService.isValidEmail(to)) {
                    throw InvalidEmailException
                }
                getUserMailContent(type, to, code, localId, accountInfo)
            } catch (e: Exception) {
                val status = if (e == TooManyVerificationCodeRequestException) MailLogStatus.THROTTLED else MailLogStatus.FAILED
                recordMailLog(type, status, to, e)
                throw e
            }

        val pending =
            mailLogRepository.save(
                MailLog(type = type, status = MailLogStatus.PENDING, email = to, subject = subject, body = body),
            )

        val outcome =
            try {
                mailClient.sendMail(to, subject, body)
            } catch (e: Exception) {
                complete(pending, MailLogStatus.FAILED, e)
                throw e
            }

        val status =
            when (outcome) {
                MailSendOutcome.SENT -> MailLogStatus.SENT
                MailSendOutcome.SUPPRESSED -> MailLogStatus.SUPPRESSED
            }
        complete(pending, status, error = null)
    }

    override suspend fun recordMailLog(
        type: UserMailType,
        status: MailLogStatus,
        email: String,
        error: Throwable?,
    ) {
        mailLogRepository.save(MailLog(type = type, status = status, email = email, errorMessage = error?.toString()))
    }

    override suspend fun getMailLogs(email: String): List<MailLog> = mailLogRepository.findAllByEmail(email.trim())

    private suspend fun complete(
        pending: MailLog,
        status: MailLogStatus,
        error: Throwable?,
    ) {
        try {
            mailLogRepository.save(pending.copy(status = status, errorMessage = error?.toString(), updatedAt = Instant.now()))
        } catch (e: Exception) {
            log.error("mail log 결과 갱신 실패: id={} status={}", pending.id, status, e)
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
