package com.wafflestudio.snu4t.common.dto

data class ListResponse<T> (
    val content: List<T>,
    val totalCount: Int?,
    val nextPageToken: String?,
)
