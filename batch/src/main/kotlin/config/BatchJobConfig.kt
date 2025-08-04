package com.wafflestudio.snutt.config

import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import javax.sql.DataSource

@Configuration
class BatchJobConfig {
    @Bean("batchDataSource")
    @BatchDataSource
    fun batchDataSource(): DataSource =
        EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("/org/springframework/batch/core/schema-h2.sql")
            .generateUniqueName(true)
            .build()
}
