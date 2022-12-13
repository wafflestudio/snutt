package com.wafflestudio.snu4t.common.exception

enum class AppType {
    RELEASE,
    DEBUG;

    companion object {
        private val ENUM_MAP: Map<String, AppType> = AppType.values().associateBy { it.toString().lowercase() }
        fun fromString(appType: String?): AppType? = appType?.let { ENUM_MAP[appType.lowercase()] }
    }
}