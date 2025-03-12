package com.wafflestudio.snutt.evaluation.dto

data class EvErrorResponse(
    val error: EvErrorInfo,
)

data class EvErrorInfo(
    val code: Long,
    val message: String,
)
