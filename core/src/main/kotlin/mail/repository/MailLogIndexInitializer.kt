package com.wafflestudio.snutt.mail.repository

import com.wafflestudio.snutt.mail.data.MailLog
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.IndexResolver
import org.springframework.stereotype.Component

@Component
class MailLogIndexInitializer(
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun createIndexes() {
        val indexOps = mongoTemplate.indexOps(MailLog::class.java)
        try {
            IndexResolver
                .create(mongoTemplate.converter.mappingContext)
                .resolveIndexFor(MailLog::class.java)
                .forEach { indexOps.createIndex(it).block() }
        } catch (e: Exception) {
            log.error("mail log 인덱스 생성 실패", e)
        }
    }
}
