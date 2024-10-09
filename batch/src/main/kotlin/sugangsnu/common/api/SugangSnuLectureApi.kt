package com.wafflestudio.snu4t.sugangsnu.common.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SugangSnuApiConfig() {
    companion object {
        const val SUGANG_SNU_BASEURL = "https://sugang.snu.ac.kr"
    }

    @Bean
    fun sugangSnuApi(): SugangSnuApi {
        val exchangeStrategies: ExchangeStrategies =
            ExchangeStrategies.builder()
                .codecs { it.defaultCodecs().maxInMemorySize(-1) } // to unlimited memory size
                .build()

        return WebClient.builder().baseUrl(SUGANG_SNU_BASEURL)
            .exchangeStrategies(exchangeStrategies) // set exchange strategies
            .defaultHeaders {
                it.setAll(
                    mapOf(
                        "User-Agent" to
                            """
                            Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)
                            AppleWebKit/537.36 (KHTML, like Gecko)
                            Chrome/86.0.4240.80
                            Safari/537.36
                            """.trimIndent().replace("\n", " "),
                        "Referer" to "https://sugang.snu.ac.kr/sugang/cc/cc100InterfaceExcel.action",
                    ),
                )
            }
            .build().let(::SugangSnuApi)
    }
}

class SugangSnuApi(webClient: WebClient) : WebClient by webClient
