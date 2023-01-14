package com.wafflestudio.snu4t.config

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
        semesterReadConverter: SemesterReadConverter
    ): MongoCustomConversions {
        val converterList: MutableList<Converter<*, *>?> = ArrayList()
        converterList.add(semesterWriteConverter)
        converterList.add(semesterReadConverter)
        return MongoCustomConversions(converterList)
    }
}