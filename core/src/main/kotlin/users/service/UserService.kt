package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.common.exception.AuthException
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import org.springframework.stereotype.Service

interface UserService {
    suspend fun getUserByCredentialHash(credentialHash: String): User
}

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override suspend fun getUserByCredentialHash(credentialHash: String): User =
        userRepository.getUserByCredentialHash(credentialHash) ?: throw AuthException
}
