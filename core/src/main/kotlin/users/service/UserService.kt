package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.common.exception.DuplicateLocalIdException
import com.wafflestudio.snu4t.common.exception.InvalidEmailException
import com.wafflestudio.snu4t.common.exception.InvalidLocalIdException
import com.wafflestudio.snu4t.common.exception.InvalidPasswordException
import com.wafflestudio.snu4t.common.exception.WrongLocalIdException
import com.wafflestudio.snu4t.common.exception.WrongPasswordException
import com.wafflestudio.snu4t.common.exception.WrongUserTokenException
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.dto.LocalLoginRequest
import com.wafflestudio.snu4t.users.dto.LocalRegisterRequest
import com.wafflestudio.snu4t.users.dto.LoginResponse
import com.wafflestudio.snu4t.users.repository.UserRepository
import org.springframework.stereotype.Service

interface UserService {
    suspend fun getUserByCredentialHash(credentialHash: String): User

    suspend fun registerLocal(localRegisterRequest: LocalRegisterRequest): LoginResponse

    suspend fun loginLocal(localRegisterRequest: LocalLoginRequest): LoginResponse

    suspend fun update(user: User): User
}

@Service
class UserServiceImpl(
    private val authService: AuthService,
    private val userRepository: UserRepository,
) : UserService {
    override suspend fun getUserByCredentialHash(credentialHash: String): User =
        userRepository.findByCredentialHashAndActive(credentialHash, true) ?: throw WrongUserTokenException

    override suspend fun registerLocal(localRegisterRequest: LocalRegisterRequest): LoginResponse {
        val localId = localRegisterRequest.id
        val password = localRegisterRequest.password
        val email = localRegisterRequest.email

        if (!authService.isValidLocalId(localId)) throw InvalidLocalIdException
        if (!authService.isValidPassword(password)) throw InvalidPasswordException
        email?.let { if (!authService.isValidEmail(email)) throw InvalidEmailException }

        if (userRepository.existsByCredentialLocalIdAndActiveTrue(localId)) throw DuplicateLocalIdException

        val credential = authService.buildLocalCredential(localId, password)
        val credentialHash = authService.generateCredentialHash(credential)

        val user = userRepository.save(
            User(
                email = email,
                isEmailVerified = false,
                credential = credential,
                credentialHash = credentialHash,
                fcmKey = null,
            )
        )

        return LoginResponse(
            userId = user.id!!,
            token = credentialHash,
        )
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

    override suspend fun update(user: User): User {
        return userRepository.save(user)
    }
}
