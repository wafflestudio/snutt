package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.auth.SocialProvider
import com.wafflestudio.snu4t.common.cache.Cache
import com.wafflestudio.snu4t.common.cache.CacheKey
import com.wafflestudio.snu4t.common.exception.DuplicateEmailException
import com.wafflestudio.snu4t.common.exception.DuplicateLocalIdException
import com.wafflestudio.snu4t.common.exception.InvalidEmailException
import com.wafflestudio.snu4t.common.exception.InvalidLocalIdException
import com.wafflestudio.snu4t.common.exception.InvalidPasswordException
import com.wafflestudio.snu4t.common.exception.Snu4tException
import com.wafflestudio.snu4t.common.exception.UserNotFoundException
import com.wafflestudio.snu4t.common.exception.WrongLocalIdException
import com.wafflestudio.snu4t.common.exception.WrongPasswordException
import com.wafflestudio.snu4t.common.exception.WrongUserTokenException
import com.wafflestudio.snu4t.notification.service.DeviceService
import com.wafflestudio.snu4t.timetables.service.TimetableService
import com.wafflestudio.snu4t.users.data.Credential
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.dto.FacebookLoginRequest
import com.wafflestudio.snu4t.users.dto.LocalLoginRequest
import com.wafflestudio.snu4t.users.dto.LocalRegisterRequest
import com.wafflestudio.snu4t.users.dto.LoginResponse
import com.wafflestudio.snu4t.users.dto.LogoutRequest
import com.wafflestudio.snu4t.users.dto.SocialLoginRequest
import com.wafflestudio.snu4t.users.dto.UserPatchRequest
import com.wafflestudio.snu4t.users.repository.UserRepository
import org.springframework.stereotype.Service

interface UserService {
    suspend fun getUser(userId: String): User

    suspend fun patchUserInfo(userId: String, userPatchRequest: UserPatchRequest): User

    suspend fun getUserByCredentialHash(credentialHash: String): User

    suspend fun registerLocal(localRegisterRequest: LocalRegisterRequest): LoginResponse

    suspend fun loginLocal(localRegisterRequest: LocalLoginRequest): LoginResponse

    suspend fun loginFacebook(facebookLoginRequest: FacebookLoginRequest): LoginResponse

    suspend fun loginGoogle(socialLoginRequest: SocialLoginRequest): LoginResponse

    suspend fun loginKakao(socialLoginRequest: SocialLoginRequest): LoginResponse

    suspend fun logout(userId: String, logoutRequest: LogoutRequest)

    suspend fun update(user: User): User
}

@Service
class UserServiceImpl(
    private val authService: AuthService,
    private val timetableService: TimetableService,
    private val deviceService: DeviceService,
    private val userRepository: UserRepository,
    private val userNicknameService: UserNicknameService,
    private val cache: Cache,
) : UserService {
    override suspend fun getUser(userId: String): User {
        return userRepository.findByIdAndActiveTrue(userId) ?: throw UserNotFoundException
    }

    override suspend fun patchUserInfo(userId: String, userPatchRequest: UserPatchRequest): User {
        val user = getUser(userId)

        with(userPatchRequest) {
            nickname?.trim()?.let {
                val prevNickname = userNicknameService.getNicknameDto(user.nickname!!).nickname
                if (it != prevNickname) {
                    user.nickname = userNicknameService.appendNewTag(it)
                }
            }
        }

        return userRepository.save(user)
    }

    override suspend fun getUserByCredentialHash(credentialHash: String): User =
        userRepository.findByCredentialHashAndActive(credentialHash, true) ?: throw WrongUserTokenException

    override suspend fun registerLocal(localRegisterRequest: LocalRegisterRequest): LoginResponse {
        val localId = localRegisterRequest.id
        val password = localRegisterRequest.password
        val email = localRegisterRequest.email

        val cacheKey = CacheKey.LOCK_REGISTER_LOCAL.build(localId)

        runCatching {
            if (!cache.acquireLock(cacheKey)) throw DuplicateLocalIdException

            if (!authService.isValidLocalId(localId)) throw InvalidLocalIdException
            if (!authService.isValidPassword(password)) throw InvalidPasswordException
            email?.let {
                if (!authService.isValidEmail(email)) throw InvalidEmailException
                if (userRepository.existsByEmailAndIsEmailVerifiedTrueAndActiveTrue(email)) throw DuplicateEmailException
            }

            if (userRepository.existsByCredentialLocalIdAndActiveTrue(localId)) throw DuplicateLocalIdException

            val credential = authService.buildLocalCredential(localId, password)
            return signup(credential, email)
        }.getOrElse {
            if (it is Snu4tException) cache.releaseLock(cacheKey)
            throw it
        }
    }

    override suspend fun loginLocal(localRegisterRequest: LocalLoginRequest): LoginResponse {
        val localId = localRegisterRequest.id
        val password = localRegisterRequest.password

        val user = userRepository.findByCredentialLocalIdAndActiveTrue(localId) ?: throw WrongLocalIdException

        if (!authService.isMatchedPassword(user, password)) throw WrongPasswordException

        return LoginResponse(
            userId = user.id!!,
            token = user.credentialHash,
        )
    }

    override suspend fun loginFacebook(facebookLoginRequest: FacebookLoginRequest): LoginResponse {
        val token = facebookLoginRequest.fbToken
        val oauth2UserResponse = authService.socialLoginWithAccessToken(SocialProvider.FACEBOOK, token)

        val user = userRepository.findByCredentialFbIdAndActiveTrue(oauth2UserResponse.socialId)

        if (user != null) {
            return LoginResponse(
                userId = user.id!!,
                token = user.credentialHash,
            )
        }

        val credential = authService.buildFacebookCredential(oauth2UserResponse)

        oauth2UserResponse.email?.let {
            if (userRepository.existsByEmailAndIsEmailVerifiedTrueAndActiveTrue(it)) throw DuplicateEmailException
        }

        return signup(credential, oauth2UserResponse.email)
    }

    override suspend fun loginGoogle(socialLoginRequest: SocialLoginRequest): LoginResponse {
        val token = socialLoginRequest.token
        val oauth2UserResponse = authService.socialLoginWithAccessToken(SocialProvider.GOOGLE, token)

        val user = userRepository.findByCredentialGoogleSubAndActiveTrue(oauth2UserResponse.socialId)

        if (user != null) {
            return LoginResponse(
                userId = user.id!!,
                token = user.credentialHash,
            )
        }

        checkNotNull(oauth2UserResponse.email) { "google email is null: $oauth2UserResponse" }
        if (userRepository.existsByEmailAndIsEmailVerifiedTrueAndActiveTrue(oauth2UserResponse.email)) throw DuplicateEmailException

        val credential = authService.buildGoogleCredential(oauth2UserResponse)

        return signup(credential, oauth2UserResponse.email)
    }

    override suspend fun loginKakao(socialLoginRequest: SocialLoginRequest): LoginResponse {
        val token = socialLoginRequest.token
        val oauth2UserResponse = authService.socialLoginWithAccessToken(SocialProvider.KAKAO, token)

        val user = userRepository.findByCredentialKakaoSubAndActiveTrue(oauth2UserResponse.socialId)

        if (user != null) {
            return LoginResponse(
                userId = user.id!!,
                token = user.credentialHash,
            )
        }

        checkNotNull(oauth2UserResponse.email) { "kakao email is null: $oauth2UserResponse" }
        if (userRepository.existsByEmailAndIsEmailVerifiedTrueAndActiveTrue(oauth2UserResponse.email)) throw DuplicateEmailException

        val credential = authService.buildKakaoCredential(oauth2UserResponse)

        return signup(credential, oauth2UserResponse.email)
    }

    private suspend fun signup(
        credential: Credential,
        email: String?
    ): LoginResponse {
        val credentialHash = authService.generateCredentialHash(credential)

        val randomNickname = userNicknameService.generateUniqueRandomNickname()

        val user = User(
            email = email,
            isEmailVerified = false,
            credential = credential,
            credentialHash = credentialHash,
            nickname = randomNickname,
            fcmKey = null,
        ).let { userRepository.save(it) }

        timetableService.createDefaultTable(user.id!!)

        return LoginResponse(
            userId = user.id,
            token = credentialHash,
        )
    }

    override suspend fun logout(userId: String, logoutRequest: LogoutRequest) {
        val user = userRepository.findByIdAndActiveTrue(userId) ?: throw UserNotFoundException
        deviceService.removeRegistrationId(user.id!!, logoutRequest.registrationId)
    }

    override suspend fun update(user: User): User {
        return userRepository.save(user)
    }
}
