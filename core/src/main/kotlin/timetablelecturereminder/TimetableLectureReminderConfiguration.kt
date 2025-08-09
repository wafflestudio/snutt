package com.wafflestudio.snutt.timetablelecturereminder

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
@EnableScheduling
class TimetableLectureReminderConfiguration {
    @Bean("timetableLectureReminderTaskScheduler")
    fun timetableLectureReminderTaskScheduler(): TaskScheduler =
        ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("timetable-lecture-reminder-task-scheduler-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)
            initialize()
        }
}
