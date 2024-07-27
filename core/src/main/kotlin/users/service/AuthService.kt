package com.wafflestudio.snu4t.users.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snu4t.auth.OAuth2Client
import com.wafflestudio.snu4t.auth.OAuth2UserResponse
import com.wafflestudio.snu4t.auth.SocialProvider
import com.wafflestudio.snu4t.common.exception.SocialConnectFailException
import com.wafflestudio.snu4t.users.data.Credential
import com.wafflestudio.snu4t.users.data.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface AuthService {
    fun isValidLocalId(localId: String): Boolean

    fun isValidPassword(password: String): Boolean

    fun isValidEmail(email: String): Boolean

    fun isMatchedPassword(user: User, password: String): Boolean

    fun generateCredentialHash(credential: Credential): String

    fun buildLocalCredential(localId: String, password: String): Credential

    fun buildFacebookCredential(oAuth2UserResponse: OAuth2UserResponse): Credential

    fun buildGoogleCredential(oAuth2UserResponse: OAuth2UserResponse): Credential

    fun buildKakaoCredential(oAuth2UserResponse: OAuth2UserResponse): Credential

    suspend fun socialLoginWithAccessToken(socialProvider: SocialProvider, token: String): OAuth2UserResponse
}

@Service
class AuthServiceImpl(
    private val passwordEncoder: PasswordEncoder,
    private val objectMapper: ObjectMapper,
    @Value("\${snutt.secret-key}") private val secretKey: String,
    clients: Map<String, OAuth2Client>,
) : AuthService {
    companion object {
        private val localIdRegex = """^[a-zA-Z0-9]{4,32}$""".toRegex()
        private val passwordRegex = """^(?=.*\d)(?=.*[a-zA-Z])\S{6,20}$""".toRegex()
        private val emailRegex = """^[a-zA-Z0-9]([-_.]?[a-zA-Z0-9])*@[a-zA-Z0-9]([-_.]?[a-zA-Z0-9])*.[a-zA-Z]{2,3}$""".toRegex()
    }

    private val clients = clients.mapKeys { SocialProvider.valueOf(it.key) }

    override fun isValidLocalId(localId: String) = localId.matches(localIdRegex)

    override fun isValidPassword(password: String) = password.matches(passwordRegex)

    override fun isValidEmail(email: String) = email.matches(emailRegex)

    override fun isMatchedPassword(user: User, password: String): Boolean {
        return passwordEncoder.matches(password, user.credential.localPw)
    }

    override fun generateCredentialHash(credential: Credential): String {
        val credentialString = objectMapper.writeValueAsString(credential)
        return hmacSHA256Hex(credentialString)
    }

    override fun buildLocalCredential(localId: String, password: String) = Credential(
        localId = localId,
        localPw = passwordEncoder.encode(password),
    )

    override fun buildFacebookCredential(oAuth2UserResponse: OAuth2UserResponse) = Credential(
        fbId = oAuth2UserResponse.socialId,
        fbName = oAuth2UserResponse.name,
    )

    override fun buildGoogleCredential(oAuth2UserResponse: OAuth2UserResponse) = Credential(
        googleSub = oAuth2UserResponse.socialId,
        googleEmail = oAuth2UserResponse.email,
    )

    override fun buildKakaoCredential(oAuth2UserResponse: OAuth2UserResponse) = Credential(
        kakaoSub = oAuth2UserResponse.socialId,
        kakaoEmail = oAuth2UserResponse.email,
    )

    private fun hmacSHA256Hex(data: String): String {
        val algorithm = "HmacSHA256"
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(secretKey.toByteArray(), algorithm))
        val macResult = mac.doFinal(data.toByteArray())
        return macResult.joinToString("") { "%02x".format(it) } // base16 encoding
    }

    override suspend fun socialLoginWithAccessToken(socialProvider: SocialProvider, token: String): OAuth2UserResponse {
        val oAuth2Client = checkNotNull(clients[socialProvider])

        return oAuth2Client.getMe(token) ?: throw SocialConnectFailException
    }
}
