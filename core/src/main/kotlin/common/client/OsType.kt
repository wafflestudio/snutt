package com.wafflestudio.snu4t.common.client

enum class OsType {
    IOS,
    ANDROID,
    WEB,
    UNKNOWN;

    companion object {
        private val ENUM_MAP: Map<String, OsType> = OsType.values().associateBy { it.toString().lowercase() }
        fun from(osType: String?): OsType = osType?.let { ENUM_MAP[osType.lowercase()] } ?: UNKNOWN
    }
}

// @ReadingConverter
// @Component
// class OsTypeReadConverter : Converter<String, OsType> {
//    override fun convert(source: Int): TimetableTheme {
//        return requireNotNull(TimetableTheme.from(source))
//    }
// }
//
// @WritingConverter
// @Component
// class OsTypeWriteConverter : Converter<TimetableTheme, Int> {
//    override fun convert(source: TimetableTheme): Int = source.value
// }
