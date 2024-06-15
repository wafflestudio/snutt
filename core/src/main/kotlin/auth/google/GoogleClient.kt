package com.wafflestudio.snu4t.auth.google

import com.wafflestudio.snu4t.auth.OAuth2Client
import com.wafflestudio.snu4t.auth.OAuth2UserResponse
import com.wafflestudio.snu4t.common.extension.get
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component("GOOGLE")
class GoogleClient(
    webClientBuilder: WebClient.Builder,
) : OAuth2Client {
    private val httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(3))
    private val webClient = webClientBuilder.clientConnector(ReactorClientHttpConnector(httpClient)).build()

    companion object {
        private const val USER_INFO_URI = "https://www.googleapis.com/oauth2/v1/userinfo"
    }

    override suspend fun getMeWithAuthCode(authorizationCode: String): OAuth2UserResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun getMe(token: String): OAuth2UserResponse? {
        val googleUserResponse =
            webClient.get<GoogleOAuth2UserResponse>(
                uri = USER_INFO_URI,
                headers = mapOf(HttpHeaders.AUTHORIZATION to "Bearer $token"),
            ).getOrNull()

        return googleUserResponse?.let {
            OAuth2UserResponse(
                socialId = it.id,
                name = it.name,
                email = it.email,
            )
        }
    }
}
