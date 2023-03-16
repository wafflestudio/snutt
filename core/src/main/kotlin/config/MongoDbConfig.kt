package com.wafflestudio.snu4t.config

import com.wafflestudio.snu4t.common.enum.DayOfWeekReadConverter
import com.wafflestudio.snu4t.common.enum.DayOfWeekWriteConverter
import com.wafflestudio.snu4t.common.enum.SemesterReadConverter
import com.wafflestudio.snu4t.common.enum.SemesterWriteConverter
import com.wafflestudio.snu4t.notification.data.NotificationTypeReadConverter
import com.wafflestudio.snu4t.notification.data.NotificationTypeWriteConverter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

@Configuration
class MongoDbConfig {
    @Bean
    fun customConversions(
        semesterWriteConverter: SemesterWriteConverter,
        semesterReadConverter: SemesterReadConverter,
        dayOfWeekWriteConverter: DayOfWeekWriteConverter,
        dayOfWeekReadConverter: DayOfWeekReadConverter,
        notificationTypeWriteConverter: NotificationTypeWriteConverter,
        notificationTypeReadConverter: NotificationTypeReadConverter,
    ): MongoCustomConversions {
        val converterList: List<Converter<*, *>?> = listOf(
            semesterWriteConverter,
            semesterReadConverter,
            dayOfWeekWriteConverter,
            dayOfWeekReadConverter,
            notificationTypeWriteConverter,
            notificationTypeReadConverter,
        )
        return MongoCustomConversions(converterList)
    }

    @Bean
    @ConditionalOnMissingBean(MongoConverter::class)
    fun mappingMongoConverter(
        context: MongoMappingContext,
        conversions: MongoCustomConversions
    ): MappingMongoConverter {
        val mappingConverter = MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context)
        mappingConverter.customConversions = conversions
        // _class 필드 삭제하기 위해 적용
        mappingConverter.setTypeMapper(DefaultMongoTypeMapper(null))
        return mappingConverter
    }
}
