package com.wafflestudio.snutt.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

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
        val connector =
            ReactorClientHttpConnector(
                HttpClient.create(
                    ConnectionProvider.builder("snuttev-provider")
                        .maxConnections(100)
                        .pendingAcquireTimeout(Duration.ofMillis(300))
                        .maxLifeTime(Duration.ofSeconds(3600))
                        .maxIdleTime(Duration.ofSeconds(240))
                        .lifo()
                        .build(),
                )
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, apiConfig.connectTimeout)
                    .doOnConnected { conn: Connection ->
                        conn
                            .addHandlerLast(ReadTimeoutHandler(apiConfig.readTimeout, TimeUnit.MILLISECONDS))
                            .addHandlerLast(WriteTimeoutHandler(apiConfig.readTimeout, TimeUnit.MILLISECONDS))
                    },
            )

        return WebClient.builder().clientConnector(connector).baseUrl(apiConfig.baseUrl).build()
    }
}

@Component
@ConfigurationProperties(prefix = "api")
class ApiConfigs {
    var server: Map<String, ApiConfig> = hashMapOf()
}

class ApiConfig {
    var readTimeout = 3000L
    var connectTimeout = 3000
    lateinit var baseUrl: String
}

class SnuttEvWebClient(webClient: WebClient) : WebClient by webClient
