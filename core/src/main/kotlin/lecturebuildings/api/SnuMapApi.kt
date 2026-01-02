package com.wafflestudio.snutt.lecturebuildings.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SnuMapApiConfig {
    companion object {
        const val SNU_MAP_BASE_URL = "https://map.snu.ac.kr"
    }

    @Bean
    fun snuMapSnuApi(): SnuMapApi {
        val exchangeStrategies: ExchangeStrategies =
            ExchangeStrategies
                .builder()
                .codecs { it.defaultCodecs().maxInMemorySize(-1) } // to unlimited memory size
                .build()

        return WebClient
            .builder()
            .baseUrl(SNU_MAP_BASE_URL)
            .exchangeStrategies(exchangeStrategies)
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
                        "Referer" to "https://map.snu.ac.kr/web/main.action",
                    ),
                )
            }.build()
            .let(::SnuMapApi)
    }
}

class SnuMapApi(
    webClient: WebClient,
) : WebClient by webClient
