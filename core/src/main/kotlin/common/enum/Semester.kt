package com.wafflestudio.snu4t.common.enum

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

enum class Semester(
    @JsonValue
    val value: Int,
    val fullName: String,
) {
    SPRING(1, "1학기"),
    SUMMER(2, "여름학기"),
    AUTUMN(3, "2학기"),
    WINTER(4, "겨울학기"),
    ;

    companion object {
        private val valueMap = entries.associateBy { e -> e.value }

        fun getOfValue(semesterValue: Int): Semester? = valueMap[semesterValue]
    }
}

@ReadingConverter
@Component
class SemesterReadConverter : Converter<Int, Semester> {
    override fun convert(source: Int): Semester = Semester.getOfValue(source)!!
}

@ReadingConverter
@Component
class SemesterNumberReadConverter : Converter<Number, Semester> {
    override fun convert(source: Number): Semester = Semester.getOfValue(source.toInt())!!
}

@Component
@WritingConverter
class SemesterWriteConverter : Converter<Semester, Int> {
    override fun convert(source: Semester): Int = source.value
}
