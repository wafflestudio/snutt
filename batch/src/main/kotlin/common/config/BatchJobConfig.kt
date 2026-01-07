package com.wafflestudio.snutt.common.config

import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchJobConfig {
    @Bean
    fun transactionManager(): PlatformTransactionManager = ResourcelessTransactionManager()
}
