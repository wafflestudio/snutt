package com.wafflestudio.snu4t.common.enum

enum class UrlScheme(private val scheme: String) {

    NONE(""),
    NOTIFICATIONS("snutt://notifications"),
    ;

    fun withReferrer(referrer: String?): String {
        if (referrer.isNullOrBlank()) {
            return scheme
        }

        return "$scheme?referrer=$referrer"
    }
}
