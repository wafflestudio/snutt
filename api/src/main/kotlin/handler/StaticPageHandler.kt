package com.wafflestudio.snutt.handler
import org.springframework.core.io.ClassPathResource
import org.springframework.http.CacheControl
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.Duration

@Component
class StaticPageHandler {
    companion object {
        const val RESOURCE_PATH = "/views"
        private val memberHtml = ClassPathResource("$RESOURCE_PATH/member.html")
        private val privacyPolicyHtml = ClassPathResource("$RESOURCE_PATH/privacy_policy.html")
        private val termsOfServiceHtml = ClassPathResource("$RESOURCE_PATH/terms_of_service.html")
        private val staticResponse =
            ServerResponse.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .header("Content-Type", "text/html; charset=utf-8")
    }

    suspend fun member(): ServerResponse = staticResponse.bodyValueAndAwait(memberHtml)

    suspend fun privacyPolicy(): ServerResponse = staticResponse.bodyValueAndAwait(privacyPolicyHtml)

    suspend fun termsOfService(): ServerResponse = staticResponse.bodyValueAndAwait(termsOfServiceHtml)
}
