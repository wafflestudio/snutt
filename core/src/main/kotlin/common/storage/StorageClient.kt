package com.wafflestudio.snutt.common.storage

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.wafflestudio.snutt.config.OciConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

@Component
class StorageClient(
    authProvider: BasicAuthenticationDetailsProvider,
) {
    private val objectStorageClient =
        ObjectStorageClient
            .builder()
            .region(OciConfig.REGION)
            .build(authProvider)

    private val namespace: String by lazy {
        objectStorageClient.getNamespace(GetNamespaceRequest.builder().build()).value
    }

    companion object {
        lateinit var instance: StorageClient
            private set

        private val uploadDuration = Duration.ofMinutes(10)
        private val getDuration = Duration.ofHours(1)
        private const val ENDPOINT = "https://objectstorage.ap-chuncheon-1.oraclecloud.com"
    }

    init {
        instance = this
    }

    suspend fun generatePutSignedUris(
        storageType: StorageType,
        key: String,
    ): String =
        withContext(Dispatchers.IO) {
            val parDetails =
                CreatePreauthenticatedRequestDetails
                    .builder()
                    .name("upload-${UUID.randomUUID()}")
                    .objectName(key)
                    .accessType(AccessType.ObjectWrite)
                    .timeExpires(Date.from(Instant.now().plus(uploadDuration)))
                    .build()

            val response =
                objectStorageClient.createPreauthenticatedRequest(
                    CreatePreauthenticatedRequestRequest
                        .builder()
                        .namespaceName(namespace)
                        .bucketName(storageType.bucketName)
                        .createPreauthenticatedRequestDetails(parDetails)
                        .build(),
                )

            "$ENDPOINT${response.preauthenticatedRequest.accessUri}"
        }

    fun generateGetUri(originUri: String): String {
        val withoutScheme = originUri.substringAfter("s3://")
        val bucketName = withoutScheme.substringBefore("/")
        val key = withoutScheme.substringAfter("/")

        if (StorageType.from(bucketName).acl == Acl.PUBLIC_READ) {
            return "$ENDPOINT/n/$namespace/b/$bucketName/o/$key"
        }

        return generateGetSignedUri(bucketName, key)
    }

    private fun generateGetSignedUri(
        bucketName: String,
        key: String,
    ): String {
        val parDetails =
            CreatePreauthenticatedRequestDetails
                .builder()
                .name("download-${UUID.randomUUID()}")
                .objectName(key)
                .accessType(AccessType.ObjectRead)
                .timeExpires(Date.from(Instant.now().plus(getDuration)))
                .build()

        val response =
            objectStorageClient.createPreauthenticatedRequest(
                CreatePreauthenticatedRequestRequest
                    .builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .createPreauthenticatedRequestDetails(parDetails)
                    .build(),
            )

        return "$ENDPOINT${response.preauthenticatedRequest.accessUri}"
    }
}

fun String.toGetUri(): String = StorageClient.instance.generateGetUri(this)
