package com.wafflestudio.snu4t.common.push.dto

import com.wafflestudio.snu4t.common.enum.UrlScheme

@JvmInline
value class PushData(val payload: Map<String, String>) {
    companion object KeyNames {
        const val URL_SCHEME = "url_scheme"
    }
}

fun PushData(vararg data: Pair<String, String>): PushData =
    PushData(data.toMap())

fun PushData(urlScheme: UrlScheme, referrer: String? = null): PushData =
    PushData(PushData.URL_SCHEME to urlScheme.withReferrer(referrer))
