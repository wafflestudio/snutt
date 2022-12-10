package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    suspend fun getUserByCredentialHash(credentialHash: String): User =
        userRepository.getUserByCredentialHash(credentialHash) ?: throw AuthException
}
