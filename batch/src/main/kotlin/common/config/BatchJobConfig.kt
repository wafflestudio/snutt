package com.wafflestudio.snutt.common.config

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@RegisterReflectionForBinding(org.h2.Driver::class)
class BatchJobConfig {
    @Bean("batchDataSource")
    fun batchDataSource(): DataSource =
        EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("/org/springframework/batch/core/schema-h2.sql")
            .generateUniqueName(true)
            .build()

    @Bean
    fun transactionManager(batchDataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(batchDataSource)
}
