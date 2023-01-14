package com.wafflestudio.snu4t

import com.wafflestudio.snu4t.MigrationTestConfig.MigrationContext
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient

@EnableConfigurationProperties(MigrationContext::class)
@Configuration
class MigrationTestConfig(
    private val context: MigrationContext,
) {
    @Bean
    fun legacyServer(): LegacyServer =
        WebClient.builder()
            .baseUrl("https://snutt-api-dev.wafflestudio.com")
            .defaultHeaders {
                it.setAll(
                    mapOf(
                        "x-access-apikey" to context.apiKey,
                        "x-access-token" to context.token
                    )
                )
            }
            .build()
            .let(::LegacyServer)

    @Bean
    fun timetableServer(applicationContext: ApplicationContext): TimetableServer =
        WebTestClient.bindToApplicationContext(applicationContext)
            .build()
            .mutate()
            .defaultHeaders {
                it.setAll(
                    mapOf(
                        "x-access-apikey" to context.apiKey,
                        "x-access-token" to context.token
                    )
                )
            }
            .build()
            .let(::TimetableServer)

    @ConfigurationProperties("migration.context")
    data class MigrationContext(
        val apiKey: String,
        val token: String,
    )
}

class LegacyServer(webClient: WebClient) : WebClient by webClient

class TimetableServer(webTestClient: WebTestClient) : WebTestClient by webTestClient
