package com.wafflestudio.snu4t.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class ApiWebClientConfig(
    private val apiConfigs: ApiConfigs
) {
    @Bean
    fun snuttevServer(): SnuttEvWebClient {
        return create(apiConfigs.server["snuttev"]!!).let(::SnuttEvWebClient)
    }

    private fun create(apiConfig: ApiConfig): WebClient {
        val connector = ReactorClientHttpConnector(
            HttpClient.create(
                ConnectionProvider.builder(apiConfig.baseUrl)
                    .maxConnections(500)
                    .pendingAcquireTimeout(Duration.ofMillis(100))
                    .maxLifeTime(Duration.ofSeconds(600))
                    .maxIdleTime(Duration.ofSeconds(60))
                    .build()
            )
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, apiConfig.connectTimeout) // Connection Timeout
                .doOnConnected { conn: Connection ->
                    conn
                        .addHandlerLast(ReadTimeoutHandler(apiConfig.readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(WriteTimeoutHandler(apiConfig.readTimeout, TimeUnit.MILLISECONDS))
                }
        )
        return WebClient.builder().clientConnector(connector).baseUrl(apiConfig.baseUrl).build()
    }
}

class SnuttEvWebClient(webClient: WebClient) : WebClient by webClient
