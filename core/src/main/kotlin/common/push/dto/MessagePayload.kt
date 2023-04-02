package common.push.dto

import common.enum.UrlScheme

data class MessagePayload(
    val title: String,
    val body: String,
    val urlSchemeString: String,
)

fun MessagePayload(
    title: String,
    body: String,
    urlScheme: UrlScheme = UrlScheme.NONE,
    referrer: String? = null
) = MessagePayload(
    title = title,
    body = body,
    urlSchemeString = urlScheme.withReferrer(referrer)
)
