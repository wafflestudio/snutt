package com.wafflestudio.snutt.common.dto

open class PageResponse<T>(
    val content: List<T>,
    val totalCount: Int = content.size,
    val nextPageToken: String?,
)
