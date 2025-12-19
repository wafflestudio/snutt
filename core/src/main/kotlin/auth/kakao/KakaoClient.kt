package com.wafflestudio.snutt.auth.kakao

import com.wafflestudio.snutt.auth.OAuth2Client
import com.wafflestudio.snutt.auth.OAuth2UserResponse
import com.wafflestudio.snutt.common.extension.get
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component("KAKAO")
class KakaoClient : OAuth2Client {
    private val log = LoggerFactory.getLogger(javaClass)

    private val httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(3))
    private val webClient = WebClient.builder().clientConnector(ReactorClientHttpConnector(httpClient)).build()

    companion object {
        private const val USER_INFO_URI = "https://kapi.kakao.com/v2/user/me"
    }

    override suspend fun getMe(token: String): OAuth2UserResponse? {
        val kakaoUserResponse =
            webClient
                .get<KakaoOAuth2UserResponse>(
                    uri = USER_INFO_URI,
                    headers = mapOf(HttpHeaders.AUTHORIZATION to "Bearer $token"),
                ).getOrNull()

        log.info("token=$token, kakaoUserResponse=$kakaoUserResponse")

        return kakaoUserResponse?.let {
            OAuth2UserResponse(
                socialId = it.id.toString(),
                email = it.kakaoAccount.email,
                isEmailVerified = it.kakaoAccount.isEmailVerified,
                name = null,
            )
        }
    }
}
