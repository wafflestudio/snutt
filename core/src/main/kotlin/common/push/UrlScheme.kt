package com.wafflestudio.snu4t.common.push

import com.wafflestudio.snu4t.config.Phase

enum class UrlScheme(private val url: String) {

    NOTIFICATIONS("notifications"),
    ;

    fun compileWith(
        phase: Phase,
        referrer: String? = null
    ): Compiled {
        val fullScheme = when (phase) {
            Phase.PROD -> "snutt://$url"
            else -> "snutt-dev://$url"
        }

        if (referrer.isNullOrBlank()) {
            return Compiled(fullScheme)
        }

        return Compiled("$fullScheme?referrer=$referrer")
    }

    @JvmInline
    value class Compiled(val value: String)
}
