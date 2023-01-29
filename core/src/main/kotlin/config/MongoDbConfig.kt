package com.wafflestudio.snu4t.config

import com.wafflestudio.snu4t.common.enum.DayOfWeekReadConverter
import com.wafflestudio.snu4t.common.enum.DayOfWeekWriteConverter
import com.wafflestudio.snu4t.common.enum.SemesterReadConverter
import com.wafflestudio.snu4t.common.enum.SemesterWriteConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoDbConfig {
    @Bean
    fun customConversions(
        semesterWriteConverter: SemesterWriteConverter,
        semesterReadConverter: SemesterReadConverter,
        dayOfWeekWriteConverter: DayOfWeekWriteConverter,
        dayOfWeekReadConverter: DayOfWeekReadConverter,
    ): MongoCustomConversions {
        val converterList: List<Converter<*, *>?> = listOf(
            semesterWriteConverter,
            semesterReadConverter,
            dayOfWeekWriteConverter,
            dayOfWeekReadConverter,
        )
        return MongoCustomConversions(converterList)
    }
}
