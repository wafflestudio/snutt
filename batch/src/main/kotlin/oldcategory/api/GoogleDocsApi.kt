package com.wafflestudio.snu4t.oldcategory.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient


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
        return WebClient.builder().baseUrl(GOOGLE_DOCS_BASE_URL)
            .exchangeStrategies(exchangeStrategies) // set exchange strategies
            .build().let(::GoogleDocsApi)
    }
}

class GoogleDocsApi(webClient: WebClient) : WebClient by webClient
