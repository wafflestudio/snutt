package com.wafflestudio.snutt.common

import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.stereotype.Component

@Component
class JobFailureLoggingListener : JobExecutionListener {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterJob(jobExecution: JobExecution) {
        if (jobExecution.status == BatchStatus.FAILED) {
            log.error("배치 실패")
            jobExecution.failureExceptions.forEach { ex ->
                log.error(ex.message, ex)
            }
        }
    }
}
