package com.wafflestudio.snu4t.users.repository

import com.wafflestudio.snu4t.users.data.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, String> {
    suspend fun findByIdAndActiveTrue(id: String): User?

    suspend fun findByIdInAndActiveTrue(ids: List<String>): List<User>

    suspend fun findByCredentialHashAndActive(credentialHash: String, active: Boolean): User?

    suspend fun existsByCredentialLocalIdAndActiveTrue(localId: String): Boolean

    suspend fun findByCredentialLocalIdAndActiveTrue(localId: String): User?

    fun findAllByIdIsIn(id: Flow<String>): Flow<User>
}
