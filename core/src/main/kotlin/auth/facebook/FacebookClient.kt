package com.wafflestudio.snu4t.auth.facebook

import com.wafflestudio.snu4t.auth.OAuth2Client
import com.wafflestudio.snu4t.auth.OAuth2UserResponse
import com.wafflestudio.snu4t.common.extension.get
import org.slf4j.LoggerFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component("FACEBOOK")
class FacebookClient(
    webClientBuilder: WebClient.Builder,
) : OAuth2Client {
    private val log = LoggerFactory.getLogger(javaClass)

    private val httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(3))
    private val webClient = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .build()

    companion object {
        private const val USER_INFO_URI = "https://graph.facebook.com/me"
    }

    override suspend fun getMe(token: String): OAuth2UserResponse? {
        val facebookUserResponse =
            webClient.get<FacebookOAuth2UserResponse>(
                uri = USER_INFO_URI,
                params = mapOf("access_token" to token),
            ).getOrNull()

        log.info("token=$token, facebookUserResponse=$facebookUserResponse")

        return facebookUserResponse?.let {
            OAuth2UserResponse(
                socialId = it.id,
                name = it.name,
                email = it.email,
                isEmailVerified = true,
            )
        }
    }
}
