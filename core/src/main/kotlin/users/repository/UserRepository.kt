package com.wafflestudio.snu4t.users.repository

import com.wafflestudio.snu4t.users.data.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, String> {
    suspend fun getUserByCredentialHash(credentialHash: String): User?
    suspend fun getFirstByActiveIsTrue(): User?
}
