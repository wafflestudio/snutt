package com.wafflestudio.snu4t.common.github.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GithubRestApiConfig(
    @Value("\${github.token}") private val token: String,
) {
    companion object {
        const val GITHUB_API_BASE_URI = "https://api.github.com"
    }

    @Bean
    fun githubRestApi(): GithubRestApi =
        WebClient.builder().baseUrl(GITHUB_API_BASE_URI)
            .defaultHeaders {
                it.setAll(
                    mapOf(
                        ACCEPT to "application/vnd.github.v3+json",
                        USER_AGENT to "snutt-timetable",
                        AUTHORIZATION to "token $token",
                    ),
                )
            }
            .build().let(::GithubRestApi)
}

class GithubRestApi(webClient: WebClient) : WebClient by webClient
