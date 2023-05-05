package com.wafflestudio.snu4t.common.enum

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

enum class TimetableTheme(val value: Int) {
    SNUTT(0),
    FALL(1),
    MODERN(2),
    CHERRY_BLOSSOM(3),
    ICE(4),
    LAWN(5),
    ;

    companion object {
        private val valueMap = TimetableTheme.values().associateBy { e -> e.value }
        fun from(value: Int) = valueMap[value]
    }
}

@ReadingConverter
@Component
class TimetableThemeReadConverter : Converter<Int, TimetableTheme> {
    override fun convert(source: Int): TimetableTheme {
        return requireNotNull(TimetableTheme.from(source))
    }
}

@Component
@WritingConverter
class TimetableThemeWriteConverter : Converter<TimetableTheme, Int> {
    override fun convert(source: TimetableTheme): Int = source.value
}
