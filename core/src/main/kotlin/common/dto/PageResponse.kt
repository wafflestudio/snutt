package com.wafflestudio.snu4t.common.dto

open class PageResponse<T> (
    val content: List<T>,
    val totalCount: Int = content.size,
    val nextPageToken: String?,
)
