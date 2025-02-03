package com.wafflestudio.snutt.common.enum

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

enum class BasicThemeType(
    @get:JsonValue val value: Int,
    val displayName: String,
) {
    SNUTT(0, "SNUTT"), // 구 버전 호환을 위해 커스텀 테마의 경우 사용됨
    FALL(1, "가을"),
    MODERN(2, "모던"),
    CHERRY_BLOSSOM(3, "벚꽃"),
    ICE(4, "얼음"),
    LAWN(5, "잔디"),
    ;

    companion object {
        const val COLOR_COUNT = 9

        fun from(value: Int) = entries.find { it.value == value }

        fun from(displayName: String) = entries.find { it.displayName == displayName }
    }
}

@ReadingConverter
@Component
class BasicThemeTypeReadConverter : Converter<Int, BasicThemeType> {
    override fun convert(source: Int): BasicThemeType {
        return requireNotNull(BasicThemeType.from(source))
    }
}

@Component
@WritingConverter
class BasicThemeTypeWriteConverter : Converter<BasicThemeType, Int> {
    override fun convert(source: BasicThemeType): Int = source.value
}
