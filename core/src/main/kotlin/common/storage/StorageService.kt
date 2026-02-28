package com.wafflestudio.snutt.common.storage

import com.wafflestudio.snutt.common.exception.TooManyFilesException
import com.wafflestudio.snutt.common.storage.FileExtension.JPG
import com.wafflestudio.snutt.common.storage.dto.FileUploadUriDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

        return coroutineScope {
            (1..count)
                .map {
                    async {
                        val key = "$path${UUID.randomUUID()}.${JPG.value}"
                        val fileOriginUri = "s3://${storageType.bucketName}/$key"

                        val uploadUriDeferred = async { storageClient.generatePutSignedUri(storageType, key) }
                        val fileUriDeferred = async { storageClient.generateGetUri(fileOriginUri) }

                        FileUploadUriDto(
                            uploadUri = uploadUriDeferred.await(),
                            fileOriginUri = fileOriginUri,
                            fileUri = fileUriDeferred.await(),
                        )
                    }
                }.awaitAll()
        }
    }

    suspend fun getFileUri(originUri: String): String = storageClient.generateGetUri(originUri)

    suspend fun getFileUris(originUris: List<String>): List<String> =
        coroutineScope {
            originUris.map { originUri -> async { getFileUri(originUri) } }.awaitAll()
        }
}
