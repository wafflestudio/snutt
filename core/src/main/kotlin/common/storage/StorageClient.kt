package com.wafflestudio.snutt.common.storage

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageAsyncClient
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.wafflestudio.snutt.common.extension.awaitOciCall
import com.wafflestudio.snutt.config.OciConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
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
        ObjectStorageAsyncClient
            .builder()
            .region(OciConfig.REGION)
            .build(authProvider)

    private lateinit var namespace: String

    companion object {
        private val uploadDuration = Duration.ofMinutes(10)
        private val getDuration = Duration.ofHours(1)
        private const val ENDPOINT = "https://objectstorage.ap-chuncheon-1.oraclecloud.com"
    }

    @PostConstruct
    fun initializeNamespace() =
        runBlocking {
            namespace = fetchNamespace()
        }

    suspend fun generatePutSignedUri(
        storageType: StorageType,
        key: String,
    ): String =
        createSignedUri(
            bucketName = storageType.bucketName,
            key = key,
            accessType = AccessType.ObjectWrite,
            expiresIn = uploadDuration,
            namePrefix = "upload",
        )

    suspend fun generateGetUri(originUri: String): String {
        val withoutScheme = originUri.removePrefix("s3://")
        val bucketName = withoutScheme.substringBefore("/")
        val key = withoutScheme.substringAfter("/", missingDelimiterValue = "")

        return if (StorageType.from(bucketName).acl == Acl.PUBLIC_READ) {
            "$ENDPOINT/n/$namespace/b/$bucketName/o/$key"
        } else {
            createSignedUri(
                bucketName = bucketName,
                key = key,
                accessType = AccessType.ObjectRead,
                expiresIn = getDuration,
                namePrefix = "download",
            )
        }
    }

    private suspend fun fetchNamespace(): String =
        awaitOciCall { handler ->
            objectStorageClient.getNamespace(GetNamespaceRequest.builder().build(), handler)
        }.value

    private suspend fun createSignedUri(
        bucketName: String,
        key: String,
        accessType: AccessType,
        expiresIn: Duration,
        namePrefix: String,
    ): String {
        val parDetails =
            CreatePreauthenticatedRequestDetails
                .builder()
                .name("$namePrefix-${UUID.randomUUID()}")
                .objectName(key)
                .accessType(accessType)
                .timeExpires(Date.from(Instant.now().plus(expiresIn)))
                .build()

        val request =
            CreatePreauthenticatedRequestRequest
                .builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .createPreauthenticatedRequestDetails(parDetails)
                .build()
        val accessUri =
            awaitOciCall { handler -> objectStorageClient.createPreauthenticatedRequest(request, handler) }
                .preauthenticatedRequest
                .accessUri

        return "$ENDPOINT$accessUri"
    }
}
