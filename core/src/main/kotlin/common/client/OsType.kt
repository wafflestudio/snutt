package com.wafflestudio.snu4t.common.client

enum class OsType {
    IOS,
    ANDROID,
    WEB,
    UNKNOWN;

    companion object {
        private val ENUM_MAP: Map<String, OsType> = OsType.values().associateBy { it.toString().lowercase() }
        fun from(osType: String?): OsType? = osType?.let { return ENUM_MAP[osType.lowercase()] }
    }
}
