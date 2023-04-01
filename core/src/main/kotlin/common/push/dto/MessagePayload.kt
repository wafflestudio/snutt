package common.push.dto

import common.enum.UrlScheme

data class MessagePayload(
    val title: String,
    val body: String,
    private val urlScheme: UrlScheme = UrlScheme.NONE,
    private val referrer: String? = null,
) {
    val urlSchemeString: String
        get() = urlScheme.withReferrer(referrer)
}
