package common.enum

enum class UrlScheme(private val scheme: String) {

    NONE(""),
    ;

    fun withReferrer(referrer: String?): String {
        if (referrer.isNullOrBlank()) {
            return scheme
        }

        return "$scheme?referrer=$referrer"
    }
}
