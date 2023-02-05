package com.wafflestudio.snu4t.common.secret

import org.springframework.stereotype.Repository
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

interface SecretRepository {
    fun getSecretString(secretName: String): String
}

@Repository
class SecretRepositoryImpl: SecretRepository {
    companion object{
        val region: Region = Region.AP_NORTHEAST_2
    }

    override fun getSecretString(secretName: String): String {
        val client = SecretsManagerClient.builder().region(region).build()
        val request = GetSecretValueRequest.builder().secretId(secretName).build()
        return client.getSecretValue(request).secretString()
    }
}
