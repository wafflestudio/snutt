package com.wafflestudio.snutt.common.dynamiclink.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class FirebaseDynamicLinkApiConfig(
    @Value("\${http.response-timeout}") private val responseTimeout: Duration,
) {
    companion object {
        const val FIREBASE_DYNAMIC_LINK_BASE_URL = "https://firebasedynamiclinks.googleapis.com/v1/"
    }

    @Bean
    fun firebaseDynamicLinkApi(): FirebaseDynamicLinkApi {
        val httpClient = HttpClient.create().responseTimeout(responseTimeout)
        return WebClient
            .builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(FIREBASE_DYNAMIC_LINK_BASE_URL)
            .build()
            .let(::FirebaseDynamicLinkApi)
    }
}

class FirebaseDynamicLinkApi(
    webClient: WebClient,
) : WebClient by webClient
