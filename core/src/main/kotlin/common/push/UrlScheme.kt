package com.wafflestudio.snu4t.common.push

import com.wafflestudio.snu4t.config.Phase

enum class UrlScheme(
    private val protocol: Protocol,
    private val url: String,
    private val devUrl: String = url
) {
    NOTIFICATIONS(Protocol.SNUTT, "notifications"),
    ;

    fun compileWith(
        phase: Phase,
        referrer: String? = null
    ): Compiled {
        val fullScheme = when (phase) {
            Phase.PROD -> "${protocol.protocol}://$url"
            else -> "${protocol.devProtocol}://$devUrl"
        }

        if (referrer.isNullOrBlank()) {
            return Compiled(fullScheme)
        }

        return Compiled("$fullScheme?referrer=$referrer")
    }

    @JvmInline
    value class Compiled(val value: String)
}

enum class Protocol(val protocol: String, val devProtocol: String = protocol) {
    SNUTT("snutt", "snutt-dev"),
    HTTPS("https"),
    HTTP("http"),
    ;
}
