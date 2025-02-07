package com.wafflestudio.snutt.clientconfig.repository

import com.wafflestudio.snutt.clientconfig.data.ClientConfig
import com.wafflestudio.snutt.common.client.AppVersion
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ClientConfigRepository : CoroutineCrudRepository<ClientConfig, String> {
    suspend fun findByNameAndMinIosVersionAndMinAndroidVersionAndMaxIosVersionAndMaxAndroidVersion(
        name: String,
        minIosVersion: AppVersion?,
        minAndroidVersion: AppVersion?,
        maxIosVersion: AppVersion?,
        maxAndroidVersion: AppVersion?,
    ): ClientConfig?

    suspend fun findByNameOrderByCreatedAtDesc(name: String): List<ClientConfig>

    suspend fun findByNameAndId(
        name: String,
        id: String,
    ): ClientConfig?

    suspend fun deleteByNameAndId(
        name: String,
        id: String,
    ): Long
}

suspend fun ClientConfigRepository.findByNameAndVersions(
    name: String,
    minIosVersion: AppVersion?,
    minAndroidVersion: AppVersion?,
    maxIosVersion: AppVersion?,
    maxAndroidVersion: AppVersion?,
) = findByNameAndMinIosVersionAndMinAndroidVersionAndMaxIosVersionAndMaxAndroidVersion(
    name,
    minIosVersion,
    minAndroidVersion,
    maxIosVersion,
    maxAndroidVersion,
)
