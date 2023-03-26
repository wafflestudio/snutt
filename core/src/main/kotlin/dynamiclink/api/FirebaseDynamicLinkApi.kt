package com.wafflestudio.snu4t.dynamiclink.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class FirebaseDynamicLinkApiConfig(
    @Value("\${http.responseTimeout}") private val responseTimeout: Duration,
) {
    companion object {
        const val FIREBASE_DYNAMIC_LINK_BASE_URL = "https://firebasedynamiclinks.googleapis.com/v1/"
    }

    @Bean
    fun firebaseDynamicLinkApi(): FirebaseDynamicLinkApi {
        val httpClient = HttpClient.create().responseTimeout(responseTimeout)
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(FIREBASE_DYNAMIC_LINK_BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build().let(::FirebaseDynamicLinkApi)
    }
}

class FirebaseDynamicLinkApi(webClient: WebClient) : WebClient by webClient
