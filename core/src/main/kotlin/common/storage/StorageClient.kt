package com.wafflestudio.snu4t.common.storage

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import software.amazon.awssdk.regions.Region.AP_NORTHEAST_2
import software.amazon.awssdk.services.s3.internal.signing.DefaultS3Presigner
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class StorageClient {
    private val presigner = DefaultS3Presigner.builder().region(AP_NORTHEAST_2).build()

    companion object {
        lateinit var instance: StorageClient
            private set

        private val uploadDuration = Duration.ofMinutes(10)
        private val getDuration = Duration.ofHours(1)
    }

    @PostConstruct
    fun init() {
        instance = this
    }

    suspend fun generatePutSignedUris(
        storageType: StorageType,
        key: String,
    ): String {
        val request =
            PutObjectRequest.builder()
                .bucket(storageType.bucketName)
                .key(key)
                .build()

        val presignRequest =
            PutObjectPresignRequest.builder()
                .signatureDuration(uploadDuration)
                .putObjectRequest(request)
                .build()

        return presigner.presignPutObject(presignRequest).url().toString()
    }

    fun generateGetUri(originUri: String): String {
        val bucketName = originUri.substringAfter("s3://").substringBefore("/")
        val key = originUri.substringAfter(bucketName).substringAfter("/")

        if (StorageType.from(bucketName).acl == Acl.PUBLIC_READ) {
            return "https://$bucketName.s3.${AP_NORTHEAST_2.id()}.amazonaws.com/$key"
        }

        return generateGetSignedUri(bucketName, key)
    }

    private fun generateGetSignedUri(bucketName: String, key: String): String {
        val request =
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

        val presignRequest =
            GetObjectPresignRequest.builder()
                .signatureDuration(getDuration)
                .getObjectRequest(request)
                .build()

        return presigner.presignGetObject(presignRequest).url().toString()
    }
}

fun String.toGetUri(): String {
    return StorageClient.instance.generateGetUri(this)
}
