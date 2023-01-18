package com.wafflestudio.snu4t.common.enum

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

enum class Semester(val value: Int) {
    SPRING(1),
    SUMMER(2),
    AUTUMN(3),
    WINTER(4);

    companion object {
        private val valueMap = values().associateBy { e -> e.value }

        fun getOfValue(semesterValue: Int): Semester? = valueMap[semesterValue]
        fun getOrNullValueOf(semesterString: String): Semester? = runCatching { valueOf(semesterString) }.getOrNull()
    }
}

@ReadingConverter
@Component
class SemesterReadConverter : Converter<Int, Semester> {
    override fun convert(source: Int): Semester = Semester.getOfValue(source)!!
}

@Component
@WritingConverter
class SemesterWriteConverter : Converter<Semester, Int> {
    override fun convert(source: Semester): Int = source.value
}
