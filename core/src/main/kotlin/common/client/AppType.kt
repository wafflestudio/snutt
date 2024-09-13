package com.wafflestudio.snu4t.common.client

enum class AppType {
    RELEASE,
    DEBUG,
    ;

    companion object {
        private val ENUM_MAP: Map<String, AppType> = AppType.entries.associateBy { it.toString().lowercase() }

        fun from(appType: String?): AppType? = appType?.let { ENUM_MAP[appType.lowercase()] }
    }
}
