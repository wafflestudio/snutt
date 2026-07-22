package com.wafflestudio.snutt.common.client

enum class Language {
    KO,
    EN,
    ;

    companion object {
        private val ENUM_MAP: Map<String, Language> = Language.entries.associateBy { it.toString().lowercase() }

        fun from(language: String?): Language? = language?.let { ENUM_MAP[language.lowercase()] }
    }
}

/**
 * 읽기 시점 언어 선택 + 폴백. EN이면 en 우선, 없으면 ko. KO면 항상 ko.
 */
fun Language.select(
    ko: String,
    en: String?,
): String = if (this == Language.EN) en ?: ko else ko

@JvmName("selectNullable")
fun Language.select(
    ko: String?,
    en: String?,
): String? = if (this == Language.EN) en ?: ko else ko
