package com.wafflestudio.snutt.common.storage

import com.wafflestudio.snutt.common.exception.TooManyFilesException
import com.wafflestudio.snutt.common.storage.FileExtension.JPG
import com.wafflestudio.snutt.common.storage.dto.FileUploadUriDto
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StorageService(
    private val storageClient: StorageClient,
) {
    companion object {
        private const val MAX_FILE_COUNT = 10
    }

    suspend fun getUploadSignedUris(
        storageSource: StorageSource,
        count: Int,
    ): List<FileUploadUriDto> {
        if (count > MAX_FILE_COUNT) throw TooManyFilesException

        val storageType = storageSource.storageType
        val path = storageSource.path?.let { "$it/" } ?: ""

        return (1..count).map {
            val key = "$path${UUID.randomUUID()}.${JPG.value}"
            val uploadUri = storageClient.generatePutSignedUris(storageType, key)

            val fileOriginUri = "s3://${storageType.bucketName}/$key"
            val fileUri = storageClient.generateGetUri(fileOriginUri)

            FileUploadUriDto(
                uploadUri = uploadUri,
                fileOriginUri = fileOriginUri,
                fileUri = fileUri,
            )
        }
    }
}
