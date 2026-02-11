package com.wafflestudio.snutt.common.mail

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.emaildataplane.EmailDPClient
import com.oracle.bmc.emaildataplane.model.EmailAddress
import com.oracle.bmc.emaildataplane.model.Recipients
import com.oracle.bmc.emaildataplane.model.Sender
import com.oracle.bmc.emaildataplane.model.SubmitEmailDetails
import com.oracle.bmc.emaildataplane.requests.SubmitEmailRequest
import com.wafflestudio.snutt.config.OciConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MailClient(
    authProvider: BasicAuthenticationDetailsProvider,
    @Value("\${oci.email.compartment-id}") private val compartmentId: String,
) {
    private val emailClient =
        EmailDPClient
            .builder()
            .region(OciConfig.REGION)
            .build(authProvider)

    val sourceEmail = "snutt@wafflestudio.com"

    suspend fun sendMail(
        to: String,
        subject: String,
        body: String,
    ) {
        withContext(Dispatchers.IO) {
            val emailDetails =
                SubmitEmailDetails
                    .builder()
                    .sender(
                        Sender
                            .builder()
                            .senderAddress(
                                EmailAddress
                                    .builder()
                                    .email(sourceEmail)
                                    .name("SNUTT")
                                    .build(),
                            ).compartmentId(compartmentId)
                            .build(),
                    ).recipients(
                        Recipients
                            .builder()
                            .to(
                                listOf(
                                    EmailAddress
                                        .builder()
                                        .email(to)
                                        .build(),
                                ),
                            ).build(),
                    ).subject(subject)
                    .bodyHtml(body)
                    .build()

            emailClient.submitEmail(
                SubmitEmailRequest
                    .builder()
                    .submitEmailDetails(emailDetails)
                    .build(),
            )
        }
    }
}
