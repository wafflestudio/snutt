package com.wafflestudio.snutt.common.mail

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.emaildataplane.EmailDPAsyncClient
import com.oracle.bmc.emaildataplane.model.EmailAddress
import com.oracle.bmc.emaildataplane.model.Recipients
import com.oracle.bmc.emaildataplane.model.Sender
import com.oracle.bmc.emaildataplane.model.SubmitEmailDetails
import com.oracle.bmc.emaildataplane.requests.SubmitEmailRequest
import com.wafflestudio.snutt.common.extension.awaitOciCall
import com.wafflestudio.snutt.config.OciConfig
import org.springframework.stereotype.Component

@Component
class MailClient(
    authProvider: BasicAuthenticationDetailsProvider,
) {
    private val compartmentId = "ocid1.compartment.oc1..aaaaaaaaxzo4fga6br76o3e34rshtsl6alzripmgdh7f4lg4u4tzezosypaq"
    private val emailClient =
        EmailDPAsyncClient
            .builder()
            .region(OciConfig.REGION)
            .build(authProvider)

    val sourceEmail = "snutt@wafflestudio.com"

    suspend fun sendMail(
        to: String,
        subject: String,
        body: String,
    ) {
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

        val request =
            SubmitEmailRequest
                .builder()
                .submitEmailDetails(emailDetails)
                .build()

        awaitOciCall { handler -> emailClient.submitEmail(request, handler) }
    }
}
