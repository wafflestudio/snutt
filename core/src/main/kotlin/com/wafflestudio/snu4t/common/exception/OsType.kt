package com.wafflestudio.snu4t.common.exception

enum class OsType {
    IOS,
    ANDROID,
    UNKNOWN;

    companion object {
        private val ENUM_MAP: Map<String, OsType> = OsType.values().associateBy { it.toString().lowercase() }
        fun fromString(osType: String?): OsType = osType?.let { ENUM_MAP[osType.lowercase()] } ?: UNKNOWN
    }
}
