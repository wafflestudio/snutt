package com.wafflestudio.snu4t.pre2025category.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class GoogleDocsApiConfig {
    companion object {
        const val GOOGLE_DOCS_BASE_URL = "https://docs.google.com"
    }

    @Bean
    fun googleDocsApi(): GoogleDocsApi {
        val exchangeStrategies: ExchangeStrategies =
            ExchangeStrategies.builder()
                .codecs { it.defaultCodecs().maxInMemorySize(-1) } // to unlimited memory size
                .build()

        val httpClient =
            HttpClient.create()
                .followRedirect(true)
                .compress(true)

        return WebClient.builder()
            .baseUrl(GOOGLE_DOCS_BASE_URL)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .build()
            .let(::GoogleDocsApi)
    }
}

class GoogleDocsApi(webClient: WebClient) : WebClient by webClient
