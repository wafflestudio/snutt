package com.wafflestudio.snu4t.auth

enum class SocialProvider(val value: String) {
    LOCAL("local"),
    FACEBOOK("facebook"),
    APPLE("apple"),
    GOOGLE("google"),
    KAKAO("kakao"),
    ;

    companion object {
        private val mapping = entries.associateBy { e -> e.value }

        fun from(value: String): SocialProvider? = mapping[value]
    }
}
