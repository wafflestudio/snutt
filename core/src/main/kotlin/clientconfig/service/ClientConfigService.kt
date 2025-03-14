package com.wafflestudio.snutt.clientconfig.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.snutt.clientconfig.data.ClientConfig
import com.wafflestudio.snutt.clientconfig.dto.PatchConfigRequest
import com.wafflestudio.snutt.clientconfig.dto.PostConfigRequest
import com.wafflestudio.snutt.clientconfig.repository.ClientConfigRepository
import com.wafflestudio.snutt.clientconfig.repository.findByNameAndVersions
import com.wafflestudio.snutt.common.cache.Cache
import com.wafflestudio.snutt.common.cache.CacheKey
import com.wafflestudio.snutt.common.cache.get
import com.wafflestudio.snutt.common.client.AppVersion
import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.client.OsType
import com.wafflestudio.snutt.common.exception.ConfigNotFoundException
import com.wafflestudio.snutt.config.PhaseUtils
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ClientConfigService(
    private val clientConfigRepository: ClientConfigRepository,
    private val objectMapper: ObjectMapper,
    private val cache: Cache,
) {
    suspend fun getConfigs(clientInfo: ClientInfo): List<ClientConfig> {
        val (osType, appVersion) = clientInfo.osType to requireNotNull(clientInfo.appVersion)

        val phase = PhaseUtils.getPhase()
        if (!phase.isProd) {
            return getAdaptableConfigs(osType, appVersion)
        }

        val cacheKey = CacheKey.CLIENT_CONFIGS.build(osType, appVersion)
        return cache.get(cacheKey) { getAdaptableConfigs(osType, appVersion) } ?: emptyList()
    }

    private suspend fun getAdaptableConfigs(
        osType: OsType,
        appVersion: AppVersion,
    ): List<ClientConfig> {
        return clientConfigRepository.findAll()
            .toList()
            .filter { it.isAdaptable(osType, appVersion) }
            .distinctBy { it.name }
    }

    suspend fun getConfigsByName(name: String): List<ClientConfig> {
        return clientConfigRepository.findByNameOrderByCreatedAtDesc(name)
    }

    suspend fun postConfig(
        name: String,
        body: PostConfigRequest,
    ): ClientConfig {
        val value = objectMapper.writeValueAsString(body.data)

        val config =
            with(body) {
                clientConfigRepository.findByNameAndVersions(
                    name = name,
                    minIosVersion = minVersion?.iosAppVersion,
                    minAndroidVersion = minVersion?.androidAppVersion,
                    maxIosVersion = maxVersion?.iosAppVersion,
                    maxAndroidVersion = maxVersion?.androidAppVersion,
                )?.copy(value = value) ?: ClientConfig(
                    name = name,
                    value = value,
                    minIosVersion = minVersion?.iosAppVersion,
                    minAndroidVersion = minVersion?.androidAppVersion,
                    maxIosVersion = maxVersion?.iosAppVersion,
                    maxAndroidVersion = maxVersion?.androidAppVersion,
                )
            }

        return clientConfigRepository.save(config)
    }

    suspend fun deleteConfig(
        name: String,
        configId: String,
    ) {
        val deleted = clientConfigRepository.deleteByNameAndId(name, configId)
        if (deleted == 0L) throw ConfigNotFoundException
    }

    suspend fun patchConfig(
        name: String,
        configId: String,
        body: PatchConfigRequest,
    ): ClientConfig {
        val config = clientConfigRepository.findByNameAndId(name, configId) ?: throw ConfigNotFoundException

        val patchedConfig =
            with(body) {
                config.copy(
                    value = data?.let { objectMapper.writeValueAsString(it) } ?: config.value,
                    minIosVersion = minVersion?.iosAppVersion ?: config.minIosVersion,
                    minAndroidVersion = minVersion?.androidAppVersion ?: config.minAndroidVersion,
                    maxIosVersion = maxVersion?.iosAppVersion ?: config.maxIosVersion,
                    maxAndroidVersion = maxVersion?.androidAppVersion ?: config.maxAndroidVersion,
                    updatedAt = LocalDateTime.now(),
                )
            }

        return clientConfigRepository.save(patchedConfig)
    }
}
