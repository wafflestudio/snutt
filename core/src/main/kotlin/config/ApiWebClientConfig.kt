package com.wafflestudio.snu4t.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
@EnableConfigurationProperties(ApiConfigs::class)
class ApiWebClientConfig(
    private val apiConfigs: ApiConfigs,
) {
    @Bean
    @Profile("!test")
    fun snuttevServer(): SnuttEvWebClient {
        return create(apiConfigs.server["snuttev"]!!).let(::SnuttEvWebClient)
    }

    private fun create(apiConfig: ApiConfig): WebClient {
        val httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(apiConfig.responseTimeout))
        val connector = ReactorClientHttpConnector(httpClient)
        return WebClient.builder().clientConnector(connector).baseUrl(apiConfig.baseUrl).build()
    }
}

@Component
@ConfigurationProperties(prefix = "api")
class ApiConfigs {
    var server: Map<String, ApiConfig> = hashMapOf()
}

class ApiConfig {
    var responseTimeout: Long = 2000
    lateinit var baseUrl: String
}

class SnuttEvWebClient(webClient: WebClient) : WebClient by webClient
