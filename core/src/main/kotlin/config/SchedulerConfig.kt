package com.wafflestudio.snutt.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler

@Configuration
@EnableScheduling
class SchedulerConfig {
    @Bean
    fun taskScheduler(): TaskScheduler =
        SimpleAsyncTaskScheduler().apply {
            setVirtualThreads(true)
            setThreadNamePrefix("simple-async-task-scheduler-")
        }
}
