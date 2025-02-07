package com.wafflestudio.snutt.coursebook.data

data class CoursebookOfficialResponse(
    val noProxyUrl: String,
    val proxyUrl: String?,
    // 구버전 대응용 url 필드, 추후 삭제
    val url: String = proxyUrl ?: noProxyUrl,
)
