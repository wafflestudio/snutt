package com.wafflestudio.snu4t.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

@Configuration
class MongoDbConfig {
    @Bean
    fun customConversions(
        converters: List<Converter<*, *>>,
    ): MongoCustomConversions {
        return MongoCustomConversions(converters)
    }

    @Bean
    fun mappingMongoConverter(
        context: MongoMappingContext,
        conversions: MongoCustomConversions
    ): MappingMongoConverter {
        return MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context).apply {
            customConversions = conversions
            // _class 필드 삭제하기 위해 적용
            setTypeMapper(DefaultMongoTypeMapper(null))
        }
    }
}
