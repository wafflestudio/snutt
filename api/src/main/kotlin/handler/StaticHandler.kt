package com.wafflestudio.snu4t.handler
import com.wafflestudio.snu4t.middleware.Middleware
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.ClassPathResource
import org.springframework.http.CacheControl
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import java.time.Duration

@Component
class StaticHandler : ServiceHandler(Middleware.NoOp) {
    companion object {
        const val RESOURCE_PATH = "/views"
        private val member = ClassPathResource("$RESOURCE_PATH/member.html")
        private val privacy_policy = ClassPathResource("$RESOURCE_PATH/privacy_policy.html")
        private val terms_of_service = ClassPathResource("$RESOURCE_PATH/terms_of_service.html")
        private val staticResponse =
            ServerResponse.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .header("Content-Type", "text/html; charset=utf-8")
    }

    suspend fun memberPage(): ServerResponse =
        staticResponse
            .bodyValue(member)
            .awaitSingle()

    suspend fun privacyPolicyPage(): ServerResponse =
        staticResponse
            .bodyValue(privacy_policy)
            .awaitSingle()

    suspend fun termsOfServicePage(): ServerResponse =
        staticResponse
            .bodyValue(terms_of_service)
            .awaitSingle()
}
