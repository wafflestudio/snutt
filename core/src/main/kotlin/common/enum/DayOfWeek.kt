package com.wafflestudio.snu4t.common.enum

import com.fasterxml.jackson.annotation.JsonValue
import com.wafflestudio.snu4t.common.exception.Snu4tException
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

enum class DayOfWeek(
    @JsonValue
    val value: Int
) {
    MONDAY(0),
    TUESDAY(1),
    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4),
    SATURDAY(5),
    SUNDAY(6);

    companion object {
        private val valueMap = DayOfWeek.values().associateBy { e -> e.value }

        fun getOfValue(dayOfWeek: Int): DayOfWeek? = valueMap[dayOfWeek]
        fun getOfValue(dayOfWeek: Double): DayOfWeek? = valueMap[dayOfWeek.toInt()]
    }
}

@ReadingConverter
@Component
class DayOfWeekReadConverter : Converter<Any, DayOfWeek> {
    override fun convert(source: Any): DayOfWeek {
        return when (source) {
            is Int -> DayOfWeek.getOfValue(source)!!
            // TODO: node 서버 이전 이후에 전부 Int 타입으로 바꾸고 아래 double type 삭제하면 좋을 듯
            is Double -> DayOfWeek.getOfValue(source)!!
            else -> throw Snu4tException()
        }
    }
}

@Component
@WritingConverter
class DayOfWeekWriteConverter : Converter<DayOfWeek, Int> {
    override fun convert(source: DayOfWeek): Int = source.value
}
