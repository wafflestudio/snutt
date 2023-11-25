package com.wafflestudio.snu4t.common.push

import com.wafflestudio.snu4t.config.Phase
import com.wafflestudio.snu4t.config.PhaseUtils

enum class UrlScheme(
    private val protocol: Protocol,
    private val url: String,
    private val devUrl: String = url
) {
    NOTIFICATIONS(Protocol.SNUTT, "notifications"),
    VACANCY(Protocol.SNUTT, "vacancy"),
    FRIENDS(Protocol.SNUTT, "friends"),
    ;

    fun compileWith(
        referrer: String? = null
    ): Compiled {
        val phase = PhaseUtils.getPhase()
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
