package com.wafflestudio.snu4t.common.push.dto

import com.wafflestudio.snu4t.common.enum.UrlScheme

data class PushMessage(
    val title: String,
    val body: String,
    val data: PushData,
)

fun PushMessage(
    title: String,
    body: String,
    customPayload: Map<String, String>
) = PushMessage(
    title = title,
    body = body,
    data = PushData(customPayload)
)

fun PushMessage(
    title: String,
    body: String,
    urlScheme: UrlScheme = UrlScheme.NONE,
    referrer: String? = null
) = PushMessage(
    title = title,
    body = body,
    data = PushData(urlScheme = urlScheme, referrer = referrer)
)
