package com.wafflestudio.snutt.handler
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.CacheControl
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.Duration

@Component
class StaticPageHandler(private val resourceLoader: ResourceLoader) {
    companion object {
        const val RESOURCE_PATH = "/views"
        private val staticResponse =
            ServerResponse
                .ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .header("Content-Type", "text/html; charset=utf-8")
    }

    private val memberHtml: Resource by lazy {
        resourceLoader.getResource("classpath:$RESOURCE_PATH/member.html")
    }

    private val privacyPolicyHtml: Resource by lazy {
        resourceLoader.getResource("classpath:$RESOURCE_PATH/privacy_policy.html")
    }

    private val termsOfServiceHtml: Resource by lazy {
        resourceLoader.getResource("classpath:$RESOURCE_PATH/terms_of_service.html")
    }

    suspend fun member(): ServerResponse = staticResponse.bodyValueAndAwait(memberHtml)

    suspend fun privacyPolicy(): ServerResponse = staticResponse.bodyValueAndAwait(privacyPolicyHtml)

    suspend fun termsOfService(): ServerResponse = staticResponse.bodyValueAndAwait(termsOfServiceHtml)
}
