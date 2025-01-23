package com.wafflestudio.snu4t.oldcategory.repository

import com.wafflestudio.snu4t.oldcategory.api.GoogleDocsApi
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.createExceptionAndAwait

@Component
class OldCategoryRepository(
    private val googleDocsApi: GoogleDocsApi
) {
    companion object {
        const val SPREADSHEET_PATH = "/spreadsheets/d"
        const val SPREADSHEET_KEY = "/1Ok2gu7rW1VYlKmC_zSjNmcljef0kstm19P9zJ_5s_QA"
        const val EXPORT_XLSX = "/export?format=xlsx"
    }

    suspend fun fetchOldCategories(): PooledDataBuffer =
        googleDocsApi.get().uri { builder ->
                builder.run {
                    path(SPREADSHEET_PATH)
                    path(SPREADSHEET_KEY)
                    path(EXPORT_XLSX)
                    build()
                }
            }.accept(MediaType.TEXT_HTML).awaitExchange {
                if (it.statusCode().is2xxSuccessful) {
                    it.awaitBody()
                } else {
                    throw it.createExceptionAndAwait()
                }
            }
}
