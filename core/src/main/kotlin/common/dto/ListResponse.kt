package com.wafflestudio.snutt.common.dto

open class ListResponse<T>(
    val content: List<T>,
    val totalCount: Int = content.size,
)
