package com.wafflestudio.snu4t.handler
import com.wafflestudio.snu4t.middleware.Middleware
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class StaticHandler : ServiceHandler(Middleware.NoOp) {
    companion object {
        const val RESOURCE_PATH = "/views"
        private val member = ClassPathResource("$RESOURCE_PATH/member.html")
        private val privacy_policy = ClassPathResource("$RESOURCE_PATH/privacy_policy.html")
        private val terms_of_service = ClassPathResource("$RESOURCE_PATH/terms_of_service.html")
    }

    suspend fun memberPage(req: ServerRequest): ServerResponse =
        handle(req) {
            member
        }

    suspend fun privacyPolicyPage(req: ServerRequest): ServerResponse =
        handle(req) {
            privacy_policy
        }

    suspend fun termsOfServicePage(req: ServerRequest): ServerResponse =
        handle(req) {
            terms_of_service
        }
}
